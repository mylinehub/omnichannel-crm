# MYLINEHUB VoiceBridge - Realtime Voice-RAG Gateway
Author: MYLINEHUB
Platform: Java 17, Spring Boot 3, WebFlux, OkHttp WS, Reactor Netty, Asterisk ARI, OpenAI Realtime, RAG Vector Search

This service provides a full-duplex realtime audio gateway between:
- Asterisk ARI (telephone audio -> RTP)
- OpenAI Realtime AI
- RAG vector search
- MyLineHub organization validation
- MinIO or AWS S3 for recordings

It is designed for ultra low latency voice agents with Realtime AI.

------------------------------------------------------------

Key Capabilities
------------------------------------------------------------

Full Duplex Voice AI
- RTP -> AI audio streaming
- AI -> TTS audio back to caller
- Low latency audio pipeline
- Uses OpenAI gpt-4o-realtime-preview model

Asterisk ARI Integration
- Secure WSS ARI events
- Secure HTTPS REST control
- Stasis application
- ExternalMedia RTP channels

RAG (Retrieval Augmented Generation)
- Calls MyLineHub vector search
- Injects knowledge into AI session

Organization Lookup
- JWT login
- Organization validation endpoint
- Session pre-validation required

Recording
- mixed / separate_legs modes
- PCM -> WAV assembly
- Upload to MinIO or AWS S3

Observability
- Prometheus metrics
- Custom metric namespace
- Spring Boot Actuator endpoints

------------------------------------------------------------

Directory Layout
------------------------------------------------------------
voicebridge/
  src/main/java/com/mylinehub/voicebridge/
    ari/
    ai/
    rtp/
    rag/
    auth/
    recording/
    config/
    web/
    VoiceBridgeApplication.java
  src/main/resources/application.properties
  docker/Dockerfile
  docker-compose.yml
  README.md
  pom.xml

------------------------------------------------------------

Logging Levels
------------------------------------------------------------

INFO (default)
- Lifecycle events
- ARI actions
- RAG activity
- AI turn logs

DEBUG
- OpenAI realtime event dumps
- ARI websocket traffic
- HTTP debug traces
- RTP per-frame logs (very verbose)

Set in properties:
logging.level.com.mylinehub.voicebridge=DEBUG

Or environment variable:
LOGGING_LEVEL_COM_MYLINEHUB_VOICEBRIDGE=DEBUG

------------------------------------------------------------

Deployment Guides
------------------------------------------------------------

1. Deploy as JAR
------------------------------------------------------------

Build:
./mvnw -DskipTests package

Run:
export OPENAI_API_KEY=sk-xxxxx
java -jar target/voicebridge-1.0.0.jar

Ports:
- HTTPS/WSS: 8082
- HTTP/WS: 8083
- RTP: UDP 40000

------------------------------------------------------------

2. Deploy with Docker
------------------------------------------------------------

Build:
docker build -t voicebridge:latest -f docker/Dockerfile .

Run:
docker run -d \
  -p 8082:8082/tcp \
  -p 8083:8083/tcp \
  -p 40000:40000/udp \
  -e OPENAI_API_KEY=sk-xxxxx \
  --name voicebridge \
  voicebridge:latest

------------------------------------------------------------

3. Deploy with Docker Compose
------------------------------------------------------------
version: "3.9"
services:
  voicebridge:
    build:
      context: .
      dockerfile: docker/Dockerfile
    container_name: voicebridge
    ports:
      - "8082:8082/tcp"
      - "8083:8083/tcp"
      - "40000:40000/udp"
    restart: unless-stopped
    environment:
      TZ: "Asia/Kolkata"
      OPENAI_API_KEY: "sk-xxxxx"

------------------------------------------------------------

4. Kubernetes Deployment
------------------------------------------------------------

Deployment:
apiVersion: apps/v1
kind: Deployment
metadata:
  name: voicebridge
spec:
  replicas: 2
  selector:
    matchLabels:
      app: voicebridge
  template:
    metadata:
      labels:
        app: voicebridge
    spec:
      containers:
        - name: voicebridge
          image: voicebridge:latest
          env:
            - name: OPENAI_API_KEY
              valueFrom:
                secretKeyRef:
                  name: voicebridge-secret
                  key: openai_api_key
          ports:
            - containerPort: 8082
            - containerPort: 8083
            - containerPort: 40000
              protocol: UDP

Service:
apiVersion: v1
kind: Service
metadata:
  name: voicebridge
spec:
  type: LoadBalancer
  selector:
    app: voicebridge
  ports:
    - name: https
      port: 8082
    - name: http
      port: 8083
    - name: rtp
      port: 40000
      protocol: UDP

Secret:
apiVersion: v1
kind: Secret
metadata:
  name: voicebridge-secret
type: Opaque
data:
  openai_api_key: BASE64_ENCODED_KEY

------------------------------------------------------------

Developer Onboarding
------------------------------------------------------------
1. Install Java 17
2. Install Maven
3. Configure Asterisk ARI TLS
4. Configure application.properties
5. Run service
6. Trigger inbound call
7. Observe realtime AI response
8. Check MinIO or S3 for recordings

------------------------------------------------------------

Project Complete
------------------------------------------------------------
This service implements:
- Voice -> AI -> RAG -> Voice loop
- MinIO/S3 recording pipeline
- ARI call routing
- OpenAI Realtime websocket integration
- Organization validation
- Supports JAR, Docker, Docker Compose, Kubernetes
