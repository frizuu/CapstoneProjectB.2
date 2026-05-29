FROM golang:1.22-alpine

WORKDIR /app

# Install git (diperlukan untuk go mod tidy)
RUN apk add --no-cache git

COPY go.mod ./

# Download semua dependency termasuk Redis
RUN go mod download

COPY . .

# Tidy dan build
RUN go mod tidy && go build -o app

EXPOSE 8080

CMD ["./app"]
