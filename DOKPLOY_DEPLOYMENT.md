# Déploiement sur Dokploy (catchee.xyz)

Ce guide explique comment déployer l'application Quarkus sur Dokploy via catchee.xyz en utilisant le Dockerfile.

## Prérequis

- Accès à votre instance Dokploy sur catchee.xyz
- Dépôt Git accessible par Dokploy (GitHub, GitLab, etc.)
- Base de données MySQL accessible depuis Dokploy
- Certificat SSL pour newscaper.catchee.xyz (Dokploy peut gérer cela avec Traefik/Let's Encrypt)

## Étape 1 : Préparer votre dépôt Git

Assurez-vous que votre code est poussé sur un dépôt Git :

```bash
git add .
git commit -m "Configure SSL/HTTPS for deployment"
git push origin main
```

## Étape 2 : Créer une nouvelle application dans Dokploy

1. Connectez-vous à votre instance Dokploy : `https://catchee.xyz`
2. Créez un nouveau projet ou sélectionnez un projet existant
3. Cliquez sur **"New Application"** ou **"Nouvelle Application"**
4. Choisissez **"Docker"** comme type de déploiement

## Étape 3 : Configuration de l'application

### Configuration de base

| Paramètre | Valeur |
|-----------|--------|
| **Nom** | quarkus-basics |
| **Source** | Git Repository |
| **Repository URL** | URL de votre dépôt Git |
| **Branch** | main |
| **Dockerfile Path** | `./Dockerfile` |
| **Build Context** | `.` (racine du projet) |

### Ports

Configurez les ports exposés :

| Port interne | Port externe | Description |
|--------------|--------------|-------------|
| 2020 | 80 ou custom | HTTP |
| 8443 | 443 ou custom | HTTPS (si gestion interne SSL) |

**Note** : Si Dokploy gère SSL via Traefik, utilisez uniquement le port 2020 (HTTP) et laissez Traefik gérer le SSL.

## Étape 4 : Configuration des variables d'environnement

Dans Dokploy, ajoutez les variables d'environnement suivantes :

### Variables obligatoires

```env
# Database Configuration
DB_URL=jdbc:mysql://[DB_HOST]:[DB_PORT]/sakila
DB_USERNAME=votre_username
DB_PASSWORD=votre_password

# HTTP Configuration
PORT=2020

# SSL Configuration (si gestion interne)
SSL_PORT=8443
KEYSTORE_PATH=/certs/keystore.p12
KEYSTORE_PASSWORD=votre_mot_de_passe_securise

# Logging (optionnel)
LOG_SQL=false
```

### Configuration de la base de données

**Option A : Base de données Dokploy**

Si vous utilisez MySQL via Dokploy :
1. Créez un service MySQL dans Dokploy
2. Notez les informations de connexion
3. Utilisez le nom du service comme host : `DB_URL=jdbc:mysql://mysql-service:3306/sakila`

**Option B : Base de données externe**

Si vous avez une base de données externe :
```env
DB_URL=jdbc:mysql://your-external-db.com:3306/sakila
DB_USERNAME=username
DB_PASSWORD=password
```

## Étape 5 : Configuration SSL/HTTPS

### Option A : SSL géré par Traefik (Recommandé)

Dokploy utilise généralement Traefik comme reverse proxy avec gestion automatique de Let's Encrypt.

**Configuration dans Dokploy :**

1. Dans les paramètres de l'application, activez **"Enable HTTPS"**
2. Configurez le domaine : `newscaper.catchee.xyz`
3. Traefik générera automatiquement un certificat Let's Encrypt
4. L'application reçoit uniquement du trafic HTTP sur le port 2020
5. Traefik gère la terminaison SSL en amont

**Variables d'environnement pour cette option :**
```env
PORT=2020
# Pas besoin de SSL_PORT, KEYSTORE_PATH, KEYSTORE_PASSWORD
```

### Option B : SSL géré par l'application

Si vous voulez que Quarkus gère directement SSL :

1. **Créer un volume pour le certificat** :
   - Dans Dokploy, créez un volume monté sur `/certs`
   - Uploadez votre `keystore.p12` dans ce volume

2. **Variables d'environnement** :
```env
PORT=2020
SSL_PORT=8443
KEYSTORE_PATH=/certs/keystore.p12
KEYSTORE_PASSWORD=votre_mot_de_passe
```

3. **Exposer les deux ports** : 2020 et 8443

## Étape 6 : Configuration du domaine

### Dans Dokploy

1. Allez dans les paramètres de l'application
2. Section **"Domains"** ou **"Domaines"**
3. Ajoutez : `newscaper.catchee.xyz`
4. Activez **"Auto HTTPS"** si disponible

### DNS

Configurez votre DNS pour pointer vers Dokploy :

```
Type: A
Nom: newscaper.catchee.xyz
Valeur: [IP de votre serveur Dokploy]
TTL: 300
```

Ou si vous utilisez un sous-domaine de catchee.xyz :

```
Type: CNAME
Nom: newscaper
Valeur: catchee.xyz
TTL: 300
```

## Étape 7 : Configuration des volumes (optionnel)

Si vous avez besoin de persister des données ou des certificats :

| Volume Path (conteneur) | Description |
|--------------------------|-------------|
| `/certs` | Certificats SSL (si gestion interne) |
| `/deployments/logs` | Logs de l'application (optionnel) |

## Étape 8 : Health Checks

Configurez les health checks pour que Dokploy surveille l'état de l'application :

```yaml
Health Check Path: /q/health/live
Health Check Port: 2020
Health Check Interval: 30s
Health Check Timeout: 10s
Health Check Retries: 3
```

## Étape 9 : Déployer l'application

1. Vérifiez que toutes les configurations sont correctes
2. Cliquez sur **"Deploy"** ou **"Déployer"**
3. Surveillez les logs de build en temps réel
4. Attendez que le déploiement soit terminé

## Étape 10 : Vérification

### Vérifier les logs

Dans Dokploy, consultez les logs de l'application pour vérifier qu'elle démarre correctement :

```
Listening on: http://0.0.0.0:2020
```

### Tester les endpoints

```bash
# Test HTTP (si pas de redirection)
curl http://newscaper.catchee.xyz/q/health

# Test HTTPS
curl https://newscaper.catchee.xyz/q/health

# Test de l'API
curl https://newscaper.catchee.xyz/q/swagger-ui
```

### Vérifier CORS

```bash
curl -H "Origin: https://newscaper.catchee.xyz" \
     -H "Access-Control-Request-Method: GET" \
     -X OPTIONS \
     https://newscaper.catchee.xyz/q/health -v
```

Vous devriez voir les headers CORS dans la réponse :
```
Access-Control-Allow-Origin: https://newscaper.catchee.xyz
Access-Control-Allow-Credentials: true
```

## Configuration avancée

### Multi-stage deployment

Pour avoir des environnements staging/production :

1. **Staging** : `staging.newscaper.catchee.xyz`
   - Branch : `develop`
   - Variables d'environnement de staging

2. **Production** : `newscaper.catchee.xyz`
   - Branch : `main`
   - Variables d'environnement de production

### Scaling

Si votre application nécessite du scaling :

1. Dans Dokploy, configurez le nombre de réplicas
2. Dokploy gérera automatiquement le load balancing
3. Assurez-vous que votre base de données peut gérer plusieurs connexions

### Monitoring

Configurez les alertes dans Dokploy :

- CPU usage > 80%
- Memory usage > 80%
- Application non responsive
- Erreurs dans les logs

## Troubleshooting

### L'application ne démarre pas

**Vérifiez les logs** dans Dokploy :

```bash
# Erreur commune : base de données inaccessible
Failed to connect to database

# Solution : vérifier DB_URL, DB_USERNAME, DB_PASSWORD
```

### Erreur de connexion à la base de données

1. Vérifiez que la base de données est accessible depuis Dokploy
2. Testez la connectivité réseau
3. Vérifiez les credentials
4. Pour MySQL Dokploy, utilisez le nom du service comme host

### CORS errors

1. Vérifiez que `quarkus.http.cors.origins` est configuré correctement
2. Le domaine doit correspondre exactement (avec https://)
3. Redéployez après modification

### Certificat SSL invalide

Si vous utilisez Traefik/Let's Encrypt :

1. Vérifiez que le DNS pointe correctement vers Dokploy
2. Attendez quelques minutes que Let's Encrypt génère le certificat
3. Consultez les logs de Traefik dans Dokploy

### Build errors

**Erreur : Maven dependency download failed**
- Dokploy doit avoir accès à Internet pour télécharger les dépendances
- Vérifiez la configuration réseau

**Erreur : Java version mismatch**
- Le Dockerfile utilise Java 25 (eclipse-temurin:25-jdk)
- Assurez-vous que Dokploy supporte cette version

## Variables d'environnement complètes (récapitulatif)

```env
# Database
DB_URL=jdbc:mysql://mysql-service:3306/sakila
DB_USERNAME=quarkus_user
DB_PASSWORD=secure_password_here

# HTTP
PORT=2020

# SSL (si gestion interne - sinon, laisser Traefik gérer)
SSL_PORT=8443
KEYSTORE_PATH=/certs/keystore.p12
KEYSTORE_PASSWORD=changeit_to_secure_password

# Logging
LOG_SQL=false

# Java Options (optionnel - déjà dans le Dockerfile)
JAVA_OPTS=-Xmx512m -Xms256m
```

## Architecture recommandée

```
Internet
    ↓
Traefik (Dokploy)
    ↓ (SSL Termination)
    ↓ HTTPS → HTTP
Quarkus App (Port 2020)
    ↓
MySQL Database
```

Cette architecture permet à Traefik de gérer SSL/HTTPS automatiquement avec Let's Encrypt, tandis que votre application Quarkus reçoit uniquement du trafic HTTP en interne.

## Commandes utiles

### Redéployer après changements

Dans Dokploy, cliquez sur **"Redeploy"** ou utilisez l'API/CLI si disponible.

### Voir les logs en temps réel

```bash
# Via l'interface Dokploy
# Ou via Docker si vous avez accès SSH au serveur
docker logs -f [container-id]
```

### Accéder au conteneur

```bash
docker exec -it [container-id] sh
```

## Ressources

- [Documentation Dokploy](https://dokploy.com/docs)
- [Quarkus Container Images Guide](https://quarkus.io/guides/container-image)
- [Traefik Let's Encrypt](https://doc.traefik.io/traefik/https/acme/)

## Checklist de déploiement

- [ ] Code poussé sur Git
- [ ] Application créée dans Dokploy
- [ ] Variables d'environnement configurées
- [ ] Base de données créée et accessible
- [ ] DNS configuré pour newscaper.catchee.xyz
- [ ] Domaine ajouté dans Dokploy
- [ ] SSL/HTTPS activé (via Traefik ou interne)
- [ ] Health checks configurés
- [ ] Application déployée
- [ ] Endpoints testés (HTTP/HTTPS)
- [ ] CORS vérifié
- [ ] Logs vérifiés pour erreurs
- [ ] Monitoring configuré

Bon déploiement ! 🚀