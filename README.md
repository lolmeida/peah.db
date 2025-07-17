# peah.db API

API de exemplo construída com Quarkus, demonstrando uma arquitetura cloud-native moderna.

## Tecnologias Utilizadas

*   **Java 21+**
*   **Quarkus:** Framework Java Supersonic Subatomic.
*   **Maven:** Gerenciamento de dependências e build.
*   **PostgreSQL:** Banco de dados relacional.
*   **Flyway:** Ferramenta para migração de schema de banco de dados.
*   **Docker:** Containerização da aplicação.
*   **Kubernetes & Helm:** Orquestração e deploy em ambiente de nuvem.
*   **GitHub Actions:** Automação de CI/CD (Build e Deploy).

## Pré-requisitos

*   JDK 21+ (Java Development Kit)
*   Docker
*   Maven 3.9+
*   `kubectl` (para interagir com o Kubernetes)
*   `helm` (para deploy no Kubernetes)

## Como Executar (Ambiente de Desenvolvimento)

1.  **Inicie um banco de dados PostgreSQL com Docker:**
    ```bash
    docker run --rm --name peah-db-dev -e POSTGRES_USER=user -e POSTGRES_PASSWORD=password -e POSTGRES_DB=peahdb -p 5432:5432 postgres:15
    ```

2.  **Execute a aplicação em modo de desenvolvimento (com hot-reload):**
    ```bash
    ./mvnw quarkus:dev
    ```

A API estará disponível em `http://localhost:8080`.

## Como Testar

Para executar a suíte de testes automatizados:

```bash
./mvnw test
```

## Como Fazer o Build

Para compilar a aplicação e empacotá-la em um arquivo JAR:

```bash
./mvnw package
```

Para construir uma imagem Docker nativa (requer GraalVM):

```bash
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

## Como Fazer o Deploy

O deploy é gerenciado pelo Helm Chart localizado no diretório `k8s/`.

1.  **Empacote o Helm Chart:**
    ```bash
    helm package k8s/
    ```

2.  **Faça o deploy no seu cluster Kubernetes:**
    ```bash
    helm upgrade --install peah-db ./peah-db-0.1.0.tgz --namespace seu-namespace
    ```

O processo de build e deploy é automatizado via GitHub Actions. Veja os arquivos em `.github/workflows`.