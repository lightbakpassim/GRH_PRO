# GRH_PRO

Gestion des ressources humaines — Vue 3 + Spring Boot + MariaDB.

**Documentation complète :** [DOCUMENTATION.md](./DOCUMENTATION.md)

## Démarrage rapide

```bash
# Backend (exporter DB_PASSWORD)
cd BACKEND && mvn spring-boot:run

# Frontend
cd grh-pro && npm install && npm run dev
```

| Service | URL |
|---------|-----|
| Front | http://localhost:3000 |
| API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |

**Comptes test** (si `SEED_TEST_USERS=true`) : `plateforme@grh.tg` / `plateforme123` · `admin@grh.tg` / `admin123` · `dg@grh.tg` / `dgChangeMe1`
