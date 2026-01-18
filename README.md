# DSCommerce

Escola: DevSuperior  
Instrutor: Prof. Dr. Nélio Alves  

# Sobre
Repositório criado para a entrega do desafio de conclusão do módulo 5 do curso [Java Spring Professional](). 

# Recursos

- Java 25
- H2
- Maven 4.0.0
- Spring Boot 4.0.1
- Spring Security 7.0.2
- Spring Tool Suite 4 (IDE)


# Critérios de correção
[✔] Endpoints públicos GET /produts e GET /products/{id} funcionam sem necessidade de login.
[✔] Endpoint de login funcionando e retornando o token de acesso.
[✔] Endpoints privados de produto (POST/PUT/DELETE) funcionam somente para usuário ADMIN.
[✔] Endpoint GET /users/me retorna usuário logado.
[✔] Endpoints GET /orders/{id} e POST /orders funcionando 
[✔] Usuário que não é ADMIN não consegue acessar pedido que não é dele em GET /orders/{id}
[✔] Endpoint GET /categories retorna todas categorias


# Recursos Auxiliares

## Body de post orders
```json
{
    "items": [
        {
            "productId": 1,
            "quantity": 2
        },
        {
            "productId": 5,
            "quantity": 1
        }
    ]
}
```



