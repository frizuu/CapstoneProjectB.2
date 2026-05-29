from locust import HttpUser, task, between
import random
import time

HOT_USER_POOL    = 20
TOTAL_USER_POOL  = 100
HOT_TX_POOL      = 20
TOTAL_TX_POOL    = 500
HOTSET_RATIO     = 0.8

MERCHANT_CODES = [
    "NMID877996734914","NMID730380552705","NMID410678781882","NMID554840476905",
    "NMID766756374012","NMID414977017747","NMID998081392257","NMID590419266731",
    "NMID574172892090","NMID640695169700","NMID822180450690","NMID368940173084",
    "NMID284466750699","NMID621547358062","NMID953192141937","NMID492467481792",
    "NMID962221291715","NMID835688578676","NMID900599638630","NMID618108804181",
    "NMID318717689317","NMID831306127378","NMID776379199972","NMID798650020488",
    "NMID287272897843","NMID864720545091","NMID707425441444","NMID602542602539",
    "NMID278059010745","NMID526960641138","NMID346742162635","NMID133795829437",
    "NMID694246732476","NMID800212440923","NMID940308496896","NMID656749801686",
    "NMID939819958468","NMID754793021786","NMID910924227997","NMID692233250235",
    "NMID425845432518","NMID749756148405","NMID256110851956","NMID431573928941",
    "NMID284851271350","NMID195616685228","NMID163355971512","NMID392283124112",
    "NMID913368341378","NMID527653333535","NMID885721473074","NMID916804316612",
    "NMID845930708911","NMID831834112782","NMID814876041975","NMID820282367394",
    "NMID421372708774","NMID307368314940","NMID896770543061","NMID704635232902",
    "NMID164855174195","NMID382892773988","NMID487219415218","NMID489810817495",
    "NMID510203928649","NMID833284816889","NMID766931742397","NMID897980678794",
    "NMID897782173134","NMID757595896794","NMID756163841207","NMID423882055030",
    "NMID383616795433","NMID279405607466","NMID466244606639","NMID925984424177",
    "NMID185542454874","NMID625052270923","NMID976128678704","NMID317063543880",
    "NMID423253588914","NMID129078366401","NMID188935271796","NMID821325905407",
    "NMID206100727097","NMID837441527616","NMID288724019803","NMID935968314322",
    "NMID470148733574","NMID443347547588","NMID995763407045","NMID317552422088",
    "NMID635274406734","NMID248892660919","NMID967028172284","NMID208381981141",
    "NMID489586440594","NMID936184968960","NMID748656501707","NMID498967781068",
]
HOT_MERCH_POOL   = 10   # 10 merchant pertama sebagai hotset
TOTAL_MERCH_POOL = len(MERCHANT_CODES)  # 100


def pick_hot_int(hot_max, total_max):
    if random.random() < HOTSET_RATIO:
        return random.randint(1, hot_max)
    return random.randint(1, total_max)


def pick_hot_merchant():
    if random.random() < HOTSET_RATIO:
        return random.choice(MERCHANT_CODES[:HOT_MERCH_POOL])
    return random.choice(MERCHANT_CODES)


class BankUser(HttpUser):
    host = "http://app:8080"
    wait_time = between(0.05, 0.1)

    @task(10)
    def view_balance(self):
        user_id = pick_hot_int(HOT_USER_POOL, TOTAL_USER_POOL)
        self.client.get(f"/balance?user_id={user_id}", name="/balance")

    @task(2)
    def view_transactions(self):
        user_id = pick_hot_int(HOT_USER_POOL, TOTAL_USER_POOL)
        self.client.get(f"/transactions?user_id={user_id}", name="/transactions")

    @task(6)
    def view_merchant_inquiry(self):
        self.client.get(
            f"/qris/inquiry?merchant_code={pick_hot_merchant()}",
            name="/qris/inquiry"
        )

    @task(4)
    def view_merchant_balance(self):
        merchant_id = pick_hot_int(1, TOTAL_MERCH_POOL)
        self.client.get(f"/merchant/balance?merchant_id={merchant_id}", name="/merchant/balance")

    @task(5)
    def view_transaction_status(self):
        tx_id = pick_hot_int(HOT_TX_POOL, TOTAL_TX_POOL)
        self.client.get(f"/transaction/status?transaction_id={tx_id}", name="/transaction/status")

    @task(1)
    def do_payment(self):
        user_id     = pick_hot_int(HOT_USER_POOL, TOTAL_USER_POOL)
        merchant_id = pick_hot_int(1, TOTAL_MERCH_POOL)
        self.client.post("/payment", json={
            "user_id": user_id, "amount": 1000, "merchant_id": merchant_id
        }, name="/payment")


class CacheWarmupChecker(HttpUser):
    host      = "http://app:8080"
    wait_time = between(1, 2)
    weight    = 1

    def _double_hit(self, url, name):
        self.client.get(url, name=f"/cache-check{name}")
        time.sleep(0.01)
        self.client.get(url, name=f"/cache-check{name}")

    @task(3)
    def check_balance_cache(self):
        self._double_hit(f"/balance?user_id={random.randint(1, HOT_USER_POOL)}", "/balance")

    @task(2)
    def check_merchant_inquiry_cache(self):
        self._double_hit(
            f"/qris/inquiry?merchant_code={random.choice(MERCHANT_CODES[:HOT_MERCH_POOL])}",
            "/qris/inquiry"
        )

    @task(2)
    def check_merchant_balance_cache(self):
        self._double_hit(f"/merchant/balance?merchant_id={random.randint(1, HOT_MERCH_POOL)}", "/merchant/balance")

    @task(2)
    def check_tx_status_cache(self):
        self._double_hit(f"/transaction/status?transaction_id={random.randint(1, HOT_TX_POOL)}", "/transaction/status")

    @task(1)
    def check_transactions_cache(self):
        self._double_hit(f"/transactions?user_id={random.randint(1, HOT_USER_POOL)}", "/transactions")