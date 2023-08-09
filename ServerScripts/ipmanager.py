from flask import Flask, request
import subprocess
import re

app = Flask(__name__)

# Create a dictionary to store IP addresses and associated websites
ip_website_map = {}

def update_nginx_config(website, listOfIps):
    nginx_config_path = '/etc/nginx/sites-available/'+website  # Replace with the actual path

    try:
        # Read the current Nginx config file
        with open(nginx_config_path, 'r') as config_file:
            config_content = config_file.read()

        pattern = r"proxy_pass http://\[(.*?);"

        # Update the IP addresses in the config_content
        # for website, listOfIps in ip_website_map.items():
        #     ips = " ".join(ip_list)
        #     config_content = config_content.replace(f"proxy_pass http://{website};", f"proxy_pass http://{ips};")

        config_content = re.sub(pattern, f"proxy_pass http://[{listOfIps[0]}]:5000;", config_content)

        # Write the updated configuration back to the Nginx config file
        with open(nginx_config_path, 'w') as config_file:
            config_file.write(config_content)

        # After updating the config, restart Nginx
        subprocess.run(['cp', '/etc/nginx/sites-available/'+website, '/etc/nginx/sites-enabled/'+website])
        subprocess.run(['sudo', 'systemctl', 'reload', 'nginx'])
    except Exception as e:
        print(f"Error updating Nginx config: {e}")

@app.route('/ping')
def get_client_ipv6():
    client_ipv6 = request.environ.get('REMOTE_ADDR')
    website = request.args.get('website', '')

    # Check if the IP address for the website has changed
    if website in ip_website_map:
        if client_ipv6 in ip_website_map[website]:
            return f"IPv6 address for {website} hasn't changed: {client_ipv6}\n"
        else:
            # Update the IP address list for the website and trigger config update and Nginx restart
            ip_website_map[website].append(client_ipv6)
            update_nginx_config()
            return f"IPv6 address for {website} updated: {client_ipv6}\n"
    else:
        # Add the website with the new IP address to the map and trigger config update and Nginx restart
        ip_website_map[website] = [client_ipv6]
        update_nginx_config(website, ip_website_map[website])
        return f"IPv6 address for {website} added: {client_ipv6}\n"

if __name__ == '__main__':
    app.run(host='::', port=999)from flask import Flask, request
import subprocess
import re

app = Flask(__name__)

# Create a dictionary to store IP addresses and associated websites
ip_website_map = {}

def update_nginx_config(website, listOfIps):
    nginx_config_path = '/etc/nginx/sites-available/'+website  # Replace with the actual path

    try:
        # Read the current Nginx config file
        with open(nginx_config_path, 'r') as config_file:
            config_content = config_file.read()

        pattern = r"proxy_pass http://\[(.*?);"

        # Update the IP addresses in the config_content
        # for website, listOfIps in ip_website_map.items():
        #     ips = " ".join(ip_list)
        #     config_content = config_content.replace(f"proxy_pass http://{website};", f"proxy_pass http://{ips};")

        config_content = re.sub(pattern, f"proxy_pass http://[{listOfIps[0]}]:5000;", config_content)

        # Write the updated configuration back to the Nginx config file
        with open(nginx_config_path, 'w') as config_file:
            config_file.write(config_content)

        # After updating the config, restart Nginx
        subprocess.run(['cp', '/etc/nginx/sites-available/'+website, '/etc/nginx/sites-enabled/'+website])
        subprocess.run(['sudo', 'systemctl', 'reload', 'nginx'])
    except Exception as e:
        print(f"Error updating Nginx config: {e}")

@app.route('/ping')
def get_client_ipv6():
    client_ipv6 = request.environ.get('REMOTE_ADDR')
    website = request.args.get('website', '')

    # Check if the IP address for the website has changed
    if website in ip_website_map:
        if client_ipv6 in ip_website_map[website]:
            return f"IPv6 address for {website} hasn't changed: {client_ipv6}\n"
        else:
            # Update the IP address list for the website and trigger config update and Nginx restart
            ip_website_map[website].append(client_ipv6)
            update_nginx_config()
            return f"IPv6 address for {website} updated: {client_ipv6}\n"
    else:
        # Add the website with the new IP address to the map and trigger config update and Nginx restart
        ip_website_map[website] = [client_ipv6]
        update_nginx_config(website, ip_website_map[website])
        return f"IPv6 address for {website} added: {client_ipv6}\n"

if __name__ == '__main__':
    app.run(host='::', port=999)