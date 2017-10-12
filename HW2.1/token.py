import requests
import os

# make sure you configure these three variables correctly before you try to run the code 
AZURE_ENDPOINT_URL='https://login.microsoftonline.com/c98a79ca-5a9a-4791-a243-f06afd67464d/oauth2/token'
AZURE_APP_ID='94b2ebbc-beb6-41ae-b703-40f55a9725ec'
AZURE_APP_SECRET='painting'

def get_token_from_client_credentials(endpoint, client_id, client_secret):
    payload = {
        'grant_type': 'client_credentials',
        'client_id': client_id,
        'client_secret': client_secret,
        'resource': 'https://management.core.windows.net/',
    }
    response = requests.post(endpoint, data=payload).json()
    return response['access_token']

# test
if __name__ == '__main__':
    auth_token = get_token_from_client_credentials(endpoint=AZURE_ENDPOINT_URL,
            client_id=AZURE_APP_ID,
            client_secret=AZURE_APP_SECRET)

    print auth_token