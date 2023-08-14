import aiohttp
import asyncio

async def fetch(url, session):
    try:
        async with session.get(url, ssl=False) as response:
            # You can handle the response here if needed
            return await response.text()
    except aiohttp.ClientError as e:
        return f"Error: {e}"

async def hit_url(url, num_requests):
    async with aiohttp.ClientSession() as session:
        tasks = [fetch(url, session) for _ in range(num_requests)]
        responses = await asyncio.gather(*tasks)
        for i, response in enumerate(responses):
            print(f"Request {i + 1}")

if __name__ == "__main__":
    target_url = "http://officeinformal.com"  # Replace with your desired URL
    num_requests = 10000  # Replace with the number of requests you want to send

    asyncio.run(hit_url(target_url, num_requests))

# import requests

# def hit_url(url, num_requests):
#     for i in range(num_requests):
#         try:
#             response = requests.get(url, verify=False)
#             # You can handle the response here if needed
#             print(f"Request {i + 1} - Status Code: {response.status_code}")
#         except requests.RequestException as e:
#             print(f"Error in request {i + 1}: {e}")

# if __name__ == "__main__":
#     target_url = "http://officeinformal.com"  # Replace with your desired 

# num_requests = 100  # Replace with the number of requests you want to 

# hit_url(target_url, num_requests)

