package com.example.capstone_frontend.data

import kotlin.math.roundToInt

data class BaselineMetricsUi(
    val averageLatencyMs: Int,
    val p95LatencyMs: Int,
    val throughputReqPerSecond: Double,
    val totalHttpRequests: Double
)

object BaselineMetricsParser {

    private var previousTotalRequests: Double? = null
    private var previousTimestampMillis: Long? = null

    fun parse(
        rawMetrics: String
    ): BaselineMetricsUi {
        val durationSumSeconds = sumMetricValues(
            rawMetrics = rawMetrics,
            metricName = "baseline_http_request_duration_seconds_sum"
        )

        val durationCount = sumMetricValues(
            rawMetrics = rawMetrics,
            metricName = "baseline_http_request_duration_seconds_count"
        )

        val totalRequests = sumMetricValues(
            rawMetrics = rawMetrics,
            metricName = "baseline_http_requests_total"
        )

        val averageLatencyMs = if (durationCount > 0.0) {
            ((durationSumSeconds / durationCount) * 1000.0).roundToInt()
        } else {
            0
        }

        val p95LatencyMs = calculateP95LatencyMs(rawMetrics)

        val throughput = calculateThroughput(
            currentTotalRequests = totalRequests
        )

        return BaselineMetricsUi(
            averageLatencyMs = averageLatencyMs,
            p95LatencyMs = p95LatencyMs,
            throughputReqPerSecond = throughput,
            totalHttpRequests = totalRequests
        )
    }

    private fun sumMetricValues(
        rawMetrics: String,
        metricName: String
    ): Double {
        return rawMetrics
            .lineSequence()
            .map { it.trim() }
            .filter { line ->
                line.isNotBlank() &&
                        !line.startsWith("#") &&
                        line.startsWith(metricName)
            }
            .sumOf { line ->
                parsePrometheusValue(line)
            }
    }

    private fun parsePrometheusValue(
        line: String
    ): Double {
        val parts = line.split(Regex("\\s+"))

        if (parts.size < 2) {
            return 0.0
        }

        return parts.last().toDoubleOrNull() ?: 0.0
    }

    private fun calculateP95LatencyMs(
        rawMetrics: String
    ): Int {
        val bucketMap = mutableMapOf<Double, Double>()

        rawMetrics
            .lineSequence()
            .map { it.trim() }
            .filter { line ->
                line.isNotBlank() &&
                        !line.startsWith("#") &&
                        line.startsWith("baseline_http_request_duration_seconds_bucket")
            }
            .forEach { line ->
                val leValue = extractLeValue(line)
                val bucketValue = parsePrometheusValue(line)

                if (leValue != null) {
                    bucketMap[leValue] = (bucketMap[leValue] ?: 0.0) + bucketValue
                }
            }

        if (bucketMap.isEmpty()) {
            return 0
        }

        val sortedBuckets = bucketMap.entries
            .sortedBy { it.key }

        val totalCount = sortedBuckets.lastOrNull {
            it.key == Double.POSITIVE_INFINITY
        }?.value ?: sortedBuckets.last().value

        if (totalCount <= 0.0) {
            return 0
        }

        val target = totalCount * 0.95

        var previousLe = 0.0
        var previousCount = 0.0

        for ((le, count) in sortedBuckets) {
            if (count >= target) {
                if (le == Double.POSITIVE_INFINITY) {
                    return (previousLe * 1000.0).roundToInt()
                }

                val bucketCount = count - previousCount

                if (bucketCount <= 0.0) {
                    return (le * 1000.0).roundToInt()
                }

                val positionInBucket = (target - previousCount) / bucketCount
                val estimatedSeconds = previousLe + ((le - previousLe) * positionInBucket)

                return (estimatedSeconds * 1000.0).roundToInt()
            }

            previousLe = le
            previousCount = count
        }

        return (sortedBuckets.last().key * 1000.0).roundToInt()
    }

    private fun extractLeValue(
        line: String
    ): Double? {
        val regex = Regex("""le="([^"]+)"""")
        val match = regex.find(line) ?: return null
        val value = match.groupValues.getOrNull(1) ?: return null

        return if (value == "+Inf") {
            Double.POSITIVE_INFINITY
        } else {
            value.toDoubleOrNull()
        }
    }

    private fun calculateThroughput(
        currentTotalRequests: Double
    ): Double {
        val now = System.currentTimeMillis()

        val previousTotal = previousTotalRequests
        val previousTime = previousTimestampMillis

        previousTotalRequests = currentTotalRequests
        previousTimestampMillis = now

        if (previousTotal == null || previousTime == null) {
            return 0.0
        }

        val deltaRequests = currentTotalRequests - previousTotal
        val deltaSeconds = (now - previousTime).toDouble() / 1000.0

        if (deltaRequests <= 0.0 || deltaSeconds <= 0.0) {
            return 0.0
        }

        return deltaRequests / deltaSeconds
    }
}