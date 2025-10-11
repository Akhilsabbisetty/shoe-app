# Shoe-Sandal App (Sample)
This repo contains a minimal **Spring Boot backend**, a static **frontend**, Dockerfiles, Kubernetes manifests and a Jenkins pipeline to build and deploy via Docker Hub + Argo CD.

**Apps**
- Backend: Spring Boot REST API exposing `/api/products`
- Frontend: Static HTML/JS that fetches products from backend and shows them
- Database: Postgres in-cluster (StatefulSet + PVC)
- Orchestrator: Kubernetes manifests for frontend/backend/postgres + Ingress for `shoes.akhilsabbisetty.site`

**How to use**
1. Edit `Jenkinsfile` credentials IDs and DockerHub / JFrog / ArgoCD details.
2. Push this repo to GitHub.
3. Configure Jenkins pipeline to point to the GitHub repo and necessary credentials.
4. Make sure your Kubernetes cluster (EKS) has an ingress controller and `shoes.akhilsabbisetty.site` DNS points to the ingress/load balancer.
5. Run pipeline — it will:
   - Build backend (Maven), build/push Docker images to DockerHub
   - Build frontend static image and push to DockerHub
   - Update `k8s/` manifests (images use tags built in pipeline)
   - Trigger ArgoCD sync

**DNS instructions (quick)**
1. Get the external IP / hostname of your Kubernetes ingress (or AWS ALB). When using AWS EKS + AWS Load Balancer Controller, create an ALIAS CNAME in Route53 pointing `shoes.akhilsabbisetty.site` to the ALB hostname.
2. Wait for DNS TTL to propagate.

Files included: `backend/`, `frontend/`, `Dockerfiles`, `k8s/`, `Jenkinsfile`.

