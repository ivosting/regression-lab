"""Reproducible load generator for GET /orders.

Usage: python scripts/load.py --requests 300 --concurrency 4 --url http://localhost:8080/orders
Prints a one-line JSON summary (client-side latency) and nothing else.
"""
import argparse
import json
import statistics
import time
import urllib.request
from concurrent.futures import ThreadPoolExecutor


def hit(url: str, i: int, run_id: str):
    req = urllib.request.Request(url, headers={"X-Request-Id": f"{run_id}-{i:05d}"})
    t0 = time.perf_counter()
    with urllib.request.urlopen(req, timeout=30) as resp:
        resp.read()
        status = resp.status
    return status, (time.perf_counter() - t0) * 1000


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--url", default="http://localhost:8080/orders")
    p.add_argument("--requests", type=int, default=300)
    p.add_argument("--concurrency", type=int, default=4)
    p.add_argument("--run-id", default=f"load-{int(time.time())}")
    a = p.parse_args()

    started = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
    with ThreadPoolExecutor(a.concurrency) as pool:
        results = list(pool.map(lambda i: hit(a.url, i, a.run_id), range(a.requests)))
    lat = sorted(r[1] for r in results)
    print(json.dumps({
        "run_id": a.run_id,
        "started_at": started,
        "finished_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "requests": len(lat),
        "non_2xx": sum(1 for r in results if r[0] >= 300),
        "p50_ms": round(statistics.median(lat), 1),
        "p95_ms": round(lat[int(len(lat) * 0.95) - 1], 1),
        "max_ms": round(lat[-1], 1),
    }))


if __name__ == "__main__":
    main()
