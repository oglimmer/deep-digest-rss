# Sealed Secrets for deep-digest-rss

The Helm chart expects a Secret named `news-secrets` to exist in the target namespace.
This secret is managed via [Sealed Secrets](https://github.com/bitnami-labs/sealed-secrets) and must be created out-of-band before installing the chart.

## Required secret keys

| Key | Used by | Description |
|-----|---------|-------------|
| `SPRING_DATASOURCE_USERNAME` | backend | MariaDB username |
| `SPRING_DATASOURCE_PASSWORD` | backend | MariaDB password |
| `AUTH_ACTUATOR_PASSWORD` | backend | Password for `/actuator` endpoint |
| `AUTH_SWAGGER_PASSWORD` | backend | Password for Swagger UI |
| `AUTH_WRITE_PASSWORD` | backend, scraper, taggroupper | Password for write API access |
| `API_KEY` | backend, scraper, taggroupper | AI provider API key (OpenAI / Anthropic) |
| `AUTH_USERNAME` | auth | Username for frontend basic auth |
| `AUTH_PASSWORD` | auth | Password for frontend basic auth |
| `DISCORD_WEBHOOK_URLS` | backend | Comma-separated Discord webhook URLs for daily digest notifications |

## Creating the SealedSecret

### 1. Create a local plain-text secret (do NOT apply this to the cluster)

```bash
kubectl create secret generic news-secrets \
  --namespace default \
  --dry-run=client \
  --output yaml \
  --from-literal=SPRING_DATASOURCE_USERNAME='news-app' \
  --from-literal=SPRING_DATASOURCE_PASSWORD='<your-db-password>' \
  --from-literal=AUTH_ACTUATOR_PASSWORD='<your-actuator-password>' \
  --from-literal=AUTH_SWAGGER_PASSWORD='<your-swagger-password>' \
  --from-literal=AUTH_WRITE_PASSWORD='<your-write-password>' \
  --from-literal=AUTH_USERNAME='<your-auth-username>' \
  --from-literal=AUTH_PASSWORD='<your-auth-password>' \
  --from-literal=API_KEY='<your-ai-api-key>' \
  --from-literal=DISCORD_WEBHOOK_URLS='<webhook-url-1>,<webhook-url-2>' \
  > news-secrets-plain.yaml
```

### 2. Seal the secret with kubeseal

```bash
kubeseal \
  --format yaml \
  --controller-name sealed-secrets-controller \
  --controller-namespace kube-system \
  < news-secrets-plain.yaml \
  > helm/deep-digest-rss/templates/sealed-secret.yaml
```

This overwrites the placeholder comment file with an actual `SealedSecret` resource.

### 3. Delete the plain-text file

```bash
rm news-secrets-plain.yaml
```

### 4. Verify

```bash
kubectl get sealedsecret news-secrets
kubectl get secret news-secrets
```

## Updating a single secret value

To update one key without recreating the entire sealed secret:

```bash
# Create a secret with only the key you want to update
echo -n '<new-value>' | kubectl create secret generic news-secrets \
  --namespace default \
  --dry-run=client \
  --output yaml \
  --from-file=API_KEY=/dev/stdin \
  > news-secrets-update.yaml

# Seal it with merge-into to update just that key
kubeseal \
  --format yaml \
  --controller-name sealed-secrets-controller \
  --controller-namespace kube-system \
  --merge-into helm/deep-digest-rss/templates/sealed-secret.yaml \
  < news-secrets-update.yaml

rm news-secrets-update.yaml
```

## Installing the chart

```bash
# First time: seal the secrets (step 1-3 above), then:
helm install deep-digest-rss ./helm/deep-digest-rss

# Upgrades:
helm upgrade deep-digest-rss ./helm/deep-digest-rss
```
