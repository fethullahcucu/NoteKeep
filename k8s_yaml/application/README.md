# NoteKeep Kubernetes (local access)

The default `clusterip.yaml` service is only reachable inside the cluster. For
local access, use one of the options below after deploying the app. Any of the
service types below will load-balance traffic across the replicas in the
`Deployment` (Kubernetes Services distribute requests to matching pods).

## Option 1: LoadBalancer (cloud/local LB)

Apply the LoadBalancer service and wait for an external IP:

```bash
kubectl apply -f k8s_yaml/application/loadbalancer.yaml
kubectl -n common-metadata-namespace get svc metadata-notekeep-loadbalancer
```

Then open: `http://<external-ip>/`

> On minikube you can also run:
>
> ```bash
> minikube service -n common-metadata-namespace metadata-notekeep-loadbalancer
> ```

## Option 2: NodePort (simplest local access)

Apply the NodePort service and access the app via the node IP:

```bash
kubectl apply -f k8s_yaml/application/nodeport.yaml
kubectl get nodes -o wide
```

Then open: `http://<node-ip>:30080/`

> On minikube you can also run:
>
> ```bash
> minikube service -n common-metadata-namespace metadata-notekeep-nodeport
> ```

## Option 3: Port-forward (no node IP needed)

```bash
kubectl -n common-metadata-namespace port-forward svc/metadata-notekeep-svc 8080:80
```

Then open: `http://localhost:8080/`

## Ingress notes

The `ingress.yaml` file requires an ingress controller to be installed in your
local cluster. If you don't have one, use LoadBalancer, NodePort, or
port-forwarding instead.
