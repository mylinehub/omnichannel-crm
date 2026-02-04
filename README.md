MyLineHub Omnichannel CRM – Setup & Deployment Guide
Target OS: Ubuntu 24.04 LTS
Deployment Type: Production / Self-Hosted
Repository Type: Monorepo (Backend, Frontend, AI, Voice)

Overview
MyLineHub is an open-source omnichannel CRM ecosystem designed to handle customer engagement across voice, email, web, and chat channels.

This document provides end-to-end deployment instructions for setting up the complete MyLineHub ecosystem on an Ubuntu 24.04 server.

Security Notice:

This repository does NOT ship production secrets.
Passwords shown are examples only.
Use environment variables or secure secret management in production.
Repository Structure
mylinehub-crm-frontend Angular frontend
mylinehub-crm Spring Boot backend
mylinehub-ai-email AI email service
mylinehub-voicebridge Voice / WebRTC / SIP services

1. Add User to sudoers (Optional)
sudo nano /etc/sudoers

Add: username ALL=(ALL) NOPASSWD:ALL

2. Configure DNS (GoDaddy)
Create an A record: app.mylinehub.com → SERVER_PUBLIC_IP

3. Firewall Setup (UFW)
sudo apt install ufw sudo ufw enable sudo ufw allow 80 sudo ufw allow 443 sudo ufw allow 8080 sudo ufw allow 8081 sudo ufw allow 5432

4. Install PostgreSQL 16
sudo apt update sudo apt install postgresql-16

5. Configure PostgreSQL External Access
Edit postgresql.conf: listen_addresses = '*'

Edit pg_hba.conf: host all all 0.0.0.0/0 md5

Restart: sudo systemctl restart postgresql

6. Install NGINX
sudo apt update sudo apt install nginx sudo nginx -t sudo systemctl restart nginx

7. Install Java 17
sudo apt install openjdk-17-jdk

8. Install NodeJS & Angular
curl -sL https://deb.nodesource.com/setup_14.x | sudo -E bash - sudo apt install -y nodejs npm install -g @angular/cli@11.2.5

9. SSL Configuration (Example)
Use Let's Encrypt or custom certificates. Convert certs to PKCS12 and JKS as required for Spring Boot.

10. Spring Boot SSL (application.properties)
server.ssl.enabled=true server.ssl.key-store=keystore.jks server.ssl.key-store-password=CHANGE_ME server.ssl.key-alias=mylinehub

11. Build Backend (Maven)
mvn clean package -Dmaven.test.skip=true

12. Run Backend
nohup java -jar crm.jar > crm-output.log 2> crm-error.log &

13. Swagger URLs
http://app.mylinehub.com:8081/swagger-ui.html https://app.mylinehub.com:8080/swagger-ui.html

14. Build & Deploy Frontend
ng build --prod Copy dist/ to /var/www/html and restart nginx

15. Logging (journalctl)
journalctl -u mylinehub-backend.service journalctl -f -u mylinehub-backend.service

16. Kill Backend (If Needed)
sudo kill -9 $(lsof -ti :8080)

Notes
Replace placeholders before production use
Restrict DB access in real environments
Use systemd services for production deployments
