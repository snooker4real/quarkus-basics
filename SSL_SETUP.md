# Configuration SSL/HTTPS pour newscaper.catchee.xyz

## Vue d'ensemble

Ce document explique comment configurer SSL/HTTPS pour l'application Quarkus afin qu'elle soit accessible via https://newscaper.catchee.xyz/.

## Configuration effectuée

La configuration SSL/HTTPS a été ajoutée dans `src/main/resources/application.properties` :

- **Port HTTPS** : 8443 (par défaut, configurable via `SSL_PORT`)
- **Type de keystore** : PKCS12
- **CORS** : Configuré pour autoriser les requêtes depuis https://newscaper.catchee.xyz

## Étapes pour obtenir et configurer le certificat SSL

### Option 1 : Let's Encrypt (Recommandé pour la production)

#### 1. Installer Certbot

```bash
# macOS
brew install certbot

# Ubuntu/Debian
sudo apt-get install certbot

# CentOS/RHEL
sudo yum install certbot
```

#### 2. Obtenir le certificat

```bash
sudo certbot certonly --standalone -d newscaper.catchee.xyz
```

Les certificats seront générés dans `/etc/letsencrypt/live/newscaper.catchee.xyz/`

#### 3. Convertir le certificat en format PKCS12

```bash
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/newscaper.catchee.xyz/fullchain.pem \
  -inkey /etc/letsencrypt/live/newscaper.catchee.xyz/privkey.pem \
  -out keystore.p12 \
  -name quarkus \
  -passout pass:VOTRE_MOT_DE_PASSE
```

#### 4. Copier le keystore dans le projet

```bash
# Pour l'inclure dans le classpath
cp keystore.p12 src/main/resources/

# OU pour le référencer en dehors du classpath
# Utilisez un chemin absolu et configurez KEYSTORE_PATH
```

### Option 2 : Certificat auto-signé (Pour développement/test uniquement)

#### 1. Générer un keystore auto-signé

```bash
keytool -genkeypair \
  -storepass changeit \
  -keyalg RSA \
  -keysize 2048 \
  -dname "CN=newscaper.catchee.xyz" \
  -alias quarkus \
  -storetype PKCS12 \
  -keystore src/main/resources/keystore.p12 \
  -validity 365
```

**Note** : Les certificats auto-signés déclencheront des avertissements de sécurité dans les navigateurs.

## Configuration des variables d'environnement

### En développement

Créez un fichier `.env` (à la racine du projet) :

```bash
SSL_PORT=8443
KEYSTORE_PATH=/chemin/absolu/vers/keystore.p12
KEYSTORE_PASSWORD=votre_mot_de_passe_securise
```

### En production (Docker/Kubernetes)

Utilisez les variables d'environnement ou les secrets :

```bash
# Exemple avec Docker
docker run \
  -e SSL_PORT=443 \
  -e KEYSTORE_PATH=/certs/keystore.p12 \
  -e KEYSTORE_PASSWORD=votre_mot_de_passe \
  -v /chemin/vers/keystore.p12:/certs/keystore.p12:ro \
  -p 443:443 \
  -p 2020:2020 \
  quarkus/quarkus-basics-jvm
```

## Activation de la redirection HTTP vers HTTPS

Pour forcer toutes les requêtes HTTP à être redirigées vers HTTPS, décommentez cette ligne dans `application.properties` :

```properties
quarkus.http.insecure-requests=redirect
```

## Configuration DNS

Assurez-vous que votre DNS pointe vers le serveur hébergeant l'application :

```
Type: A
Nom: newscaper.catchee.xyz
Valeur: [Adresse IP de votre serveur]
```

## Configuration du reverse proxy (si applicable)

Si vous utilisez Nginx ou Apache en reverse proxy :

### Nginx

```nginx
server {
    listen 80;
    server_name newscaper.catchee.xyz;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name newscaper.catchee.xyz;

    ssl_certificate /etc/letsencrypt/live/newscaper.catchee.xyz/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/newscaper.catchee.xyz/privkey.pem;

    location / {
        proxy_pass http://localhost:2020;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**Note** : Si vous utilisez un reverse proxy avec SSL/TLS termination, vous n'avez pas besoin de configurer SSL dans Quarkus. Le reverse proxy gère HTTPS et transmet les requêtes en HTTP vers Quarkus.

## Test de la configuration

### 1. Démarrer l'application

```bash
./mvnw quarkus:dev
```

### 2. Tester HTTPS

```bash
# Avec certificat auto-signé (ignore les erreurs SSL)
curl -k https://localhost:8443/q/health

# Avec certificat valide
curl https://newscaper.catchee.xyz/q/health
```

### 3. Vérifier les endpoints

- **HTTP** : http://localhost:2020/q/swagger-ui
- **HTTPS** : https://localhost:8443/q/swagger-ui
- **Production** : https://newscaper.catchee.xyz/q/swagger-ui

## Renouvellement du certificat Let's Encrypt

Les certificats Let's Encrypt expirent après 90 jours. Configurez un renouvellement automatique :

```bash
# Ajouter au crontab
sudo crontab -e

# Ajouter cette ligne pour renouveler tous les mois
0 0 1 * * certbot renew --quiet && systemctl restart quarkus-app
```

Après le renouvellement, recréez le fichier PKCS12 et redémarrez l'application.

## Sécurité

1. **Ne jamais commiter le keystore** dans Git
2. Ajouter au `.gitignore` :
   ```
   *.p12
   *.jks
   .env
   ```
3. Utiliser des gestionnaires de secrets en production (AWS Secrets Manager, HashiCorp Vault, etc.)
4. Changer le mot de passe par défaut `changeit`

## Configuration CORS

La configuration CORS est déjà en place pour autoriser les requêtes depuis https://newscaper.catchee.xyz :

- Méthodes autorisées : GET, POST, PUT, DELETE, OPTIONS
- Headers autorisés : accept, authorization, content-type, x-requested-with
- Credentials autorisés : true

Pour ajouter d'autres domaines, modifiez `quarkus.http.cors.origins` dans `application.properties`.

## Dépannage

### Erreur "keystore not found"

Vérifiez le chemin du keystore dans la variable `KEYSTORE_PATH`.

### Erreur "Incorrect password"

Assurez-vous que `KEYSTORE_PASSWORD` correspond au mot de passe utilisé lors de la création du keystore.

### Port 443 déjà utilisé

Si le port 443 est déjà utilisé (par Nginx, Apache, etc.), utilisez un autre port (8443) et configurez un reverse proxy.

### CORS errors

Vérifiez que l'origine dans `quarkus.http.cors.origins` correspond exactement à l'URL du frontend (https://newscaper.catchee.xyz).

## Ressources

- [Quarkus Security Guide](https://quarkus.io/guides/security)
- [Quarkus HTTP Reference](https://quarkus.io/guides/http-reference)
- [Let's Encrypt Documentation](https://letsencrypt.org/docs/)
- [Certbot Documentation](https://certbot.eff.org/docs/)