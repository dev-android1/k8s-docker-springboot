# Spring Boot + MySQL + Docker + Kubernetes 

## Project Overview

This project demonstrates the deployment of a Spring Boot application with two REST APIs, Docker containerization, and Kubernetes orchestration using Google cloud. The system also includes MySQL as a database running inside Kubernetes using a StatefulSet.


---

## Technologies Used

- Java 17
- Spring Boot
- MySQL 8
- Docker 
- Kubernetes(Google Kubernetes Engine)
- ConfigMap / Secret / StatefulSet / Deployment / Ingress
- Horizontal Pod Autoscaler (HPA)

---

## GIT Repository

https://github.com/dev-android1/k8s-docker-springboot


## Docker Repository

https://hub.docker.com/r/ashishdev1/springbootwithmysql

## API URL

http://nagpapp.local/users

http://nagpapp.local/welcome

http://nagpapp.local/pool-status

## API Details

### Endpoints: 
1. `GET /users`

- Returns all user records from the database.
- Example response:
```json
[
  {
    "id": 1,
    "name": "Ashish",
    "email": "ashish@gmail.com"
  }
]
```

2. `POST /user`

- create user in the database.
- Example request:
```json
{
  "name": "Ashish",
  "email": "ashish@gmail.com"
}
```

3.  `GET /welcome`
- This API is used to check for latest deployment.


4.  `GET /pool-status`
- This API is used to check for active and idle DB connection.


> You can test these API after deployment using:
```bash
curl http://<ingress-ip>/<path>
```

---

## JAVA Structure

```
src/main
├── java/
│   ├── controller/UserController.java    # 4 Rest APIs (getUser,postUser,welcome,pool-status)
│   ├── entity/User                       # Table entity
│   ├── exception/GlobalExceptionHandler  # Exception Handler
│   └── StartupLogger.java                #For Logging details on start
│
├── resources/
│   ├── application.properties            # DB configuration and connection pooling
│   ├── data.sql                          # Initial records to be inserted on app launch
│   └── schema.sql                        # Table structure
```

---

## Spring Boot Docker Build

```bash
# Build the JAR
./gradlew build

# Build Docker Image
docker build -t <yourdockerhub>/<name>:<version> .

# Push to DockerHub
docker push <yourdockerhub>/<name>:<version>

# For Google Cloud include platform if prev does not work
docker buildx build --platform linux/amd64 -t <yourdockerhub>/<name>:<version> --push .
```

---

## Kubernetes Structure

```
k8s/
├── app/
│   ├── deployment.yaml         # Spring Boot Deployment (4 replicas)
│   ├── service.yaml            # NodePort for API
│   ├── configmap.yaml          # DB URL, port, etc. (plain text info)
│   ├── secrets.yaml            # DB user/password (Base 64 encoded)
│   ├── ingress.yaml            # Ingress (for external access via rules)
│   └── hpa.yaml                # HPA definition (for load testing)
│
├── mysql/
│   ├── statefulset.yaml        # MySQL StatefulSet (stable identity, 1 pod)
│   ├── service.yaml            # Headless Service for MySQL
│   ├── configmap.yaml          # MySQL config (plain text info)
│   ├── secret.yaml             # MySQL secrets (Base64 encoded)
│   └── pvc.yaml                # Persistent Volume Claim (For persistent storage)
```

---


## Google cloud setup

### 1. Create cluster

```bash
gcloud container clusters create <cluster_name> \
--zone <region name> \
--num-nodes=2 \
--machine-type=e2-medium \
--disk-type=pd-standard \
--disk-size=30 \
--enable-ip-alias
```

### 2. For Ingress
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/cloud/deploy.yaml

```

### 3. Apply yaml files
```bash
kubectl apply -f k8s/mysql/
kubectl apply -f k8s/app/
```

### 4. Check Pods/Nodes
```bash
kubectl get pods -o wide
kubectl get nodes -o wide
```

### 5. Get ingress URL
```bash
kubectl get ingress
```

### 6. Add firewall rule to access ingress URL 

First get the target tag for firewall rule
```bash
gcloud compute instances list --filter="name~'gke-'" --format="table(name,networkInterfaces.networkIP,tags.items)"
```

Put above tag name in below command
```bash
gcloud compute firewall-rules create allow-nodeport \
--allow tcp:30000-32767 \
--target-tags=<tag name> \
--network=default \
--direction=INGRESS \
--priority=1000 \
--description="Allow NodePort access to GKE nodes"

```

After this 
```bash
kubectl get ingress
```

```bash
# Copy ingress URL and paste into host file via accessing host file (/etc/host)
sudo nano /etc/hosts
add ingress ip : nagpapp.local

# Test API with host name defined in ingress.yaml (nagpapp.local)
```

### 7. Check use cases 

### 7.1 Delete SQL pod and check data persistence on auto re-creation
```bash
# name of pod can be get using kubectl get pods
kubectl delete pod <mysql-pod-name>
```


### 7.2 Change docker image version for deployment and check rollout update
```bash
kubectl set image deployment/springboot-api <docker-repo-name>=<docker-user-name>/<docker-repo-name>:<new version>
```

### 7.3 you can check rollout update
```bash
kubectl rollout status deployment/springboot-api
```

### 8. Horizontal Pod Autoscaller

Check for metric server
```bash
kubectl get deployment metrics-server -n kube-system
```
If it is not enabled then enable it


Enable Auto scaling
```bash
gcloud container clusters update <cluster-name> \
  --zone <zone-name> \
  --enable-autoscaling \
  --min-nodes=4 \
  --max-nodes=8 \
  --node-pool=default-pool
```

Run on terminal (Load generation via hey)

```bash
# hey is a load testing tool written in Go, used to simulate a large number of HTTP requests to test the performance of an API or web service.

brew install hey

hey -z 2m -c 20 http://<external-ip>:<port>/users

or 

hey -z 2m -c 20 http://nagpapp.local/users
```
-z 2m -- Run test for 2 min


-c 20 -- Use 20 concurrent user

### 9. Delete resources after work 
```bash
kubectl delete -f k8s/app
kubectl delete -f k8s/mysql

gcloud container clusters delete <cluster-name> \
  --region <region-name>
```

------------

## Minikube Setup for local development

### 1. Start Minikube

```bash
minikube start --driver=docker
```

### 2. Enable Metrics Server (for HPA)

```bash
minikube addons enable ingress 
minikube addons enable metrics-server
```


### 3. Deploy to Kubernetes

```bash
# Deploy MySQL
kubectl apply -f k8s/mysql/

# Deploy API and HPA
kubectl apply -f k8s/app/
```


### 4. Access the API

```bash
minikube tunnel 
kubectl get ingress
```

### 5. Edit host file
127.0.0.1  nagpapp.local

Test:

```bash
curl http://nagpapp.local/users
```


### 6. Rolling Update Demo

Update the deployment:

```bash
kubectl set image deployment/springboot-api springboot-api=new-image:tag
```

Watch pods:

```bash
kubectl rollout status deployment/springboot-api
```


### 7. Data Persistence Demo

1. Run the app and insert users.
2. Delete the MySQL pod:

```bash
kubectl delete pod <mysql-pod-name>
```

3. Pod will restart, and data will be preserved (via PVC).


### 8. Test Horizontal Pod Autoscaler

Load the API

```bash
while true; do curl http://nagpapp.local/users; done
```

Or:

```bash
hey -z 2m -c 20 http://nagpapp.local/users
```

Watch Pods and HPA

```bash
watch kubectl get hpa
```

> HPA will scale up pods if CPU > 50% for sustained time. It will scale down gradually when load reduces.


## Cleanup

```bash
kubectl delete -f k8s/app
kubectl delete -f k8s/mysql
minikube stop
minikube delete
```

---

## Author

Ashish Agrawal  

---

