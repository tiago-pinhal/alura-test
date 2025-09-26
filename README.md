# Case Tecnico Alura
### Validação dos requisitos

### Via teste unitários:
```bash
./mvnw test
```

### Ou executando chamadas através do curl:
Inicialização do sistema:
```bash
./mvnw clean spring-boot:run -Dspring.profiles.active=dev
```

Para visualização dos dados no H2 Console: [h2-console](http://localhost:8080/h2-console)

### Validações

**O enunciado (`statement`) deve ter no mínimo 4 e no máximo 255 caracteres.\
Um curso só pode receber atividades se seu status for `BULDING`**
```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/opentext \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "O que aprendemos na aula de hoje?",
        "order": 1
      }'
 ```

- Statement inválido (3 caracteres), deve retornar HTTP 400 (Statement must be between 4 and 255 characters):
```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/opentext \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "Oi?",
        "order": 2
      }'
 ```
**O curso não pode ter duas questões com o mesmo enunciado**
- Repetição de statement, deve retornar 400 (Course already has a task with this statement):
```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/opentext \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "O que aprendemos na aula de hoje?",
        "order": 2
      }'
 ```

**A ordem deve ser um número inteiro positivo**
- Ordem negativa, deve retornar 400 (Order must be a positive number):
```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/opentext \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "O que aprendemos na aula de hoje?",
        "order": -2
      }'
 ```

## Tipos de atividade

#### Atividade de Resposta Aberta
```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/opentext \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "Atividade de Resposta Aberta",
        "order": 2
      }'
 ```

#### Atividade de alternativa única
```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/singlechoice \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "Atividade de alternativa única",
        "order": 3,
        "options": [
            {
                "option": "Java",
                "isCorrect": true
            },
            {
                "option": "Python",
                "isCorrect": false
            },
            {
                "option": "Ruby",
                "isCorrect": false
            }
        ]
      }'
 ```

- A atividade deve ter no minimo 2 e no máximo 5 alternativas. Abaixo, requisições inválidas.\
  Ambas retornarão HTTP 400 (Single choice task must have between 2 and 5 options)
```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/singlechoice \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "Uma única alternativa ",
        "order": 3,
        "options": [
            {
                "option": "Java",
                "isCorrect": true
            }
        ]
      }'
 ```
```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/singlechoice \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "Mais de 5 alternativas",
        "order": 3,
        "options": [
            {
                "option": "Java",
                "isCorrect": true
            },
            {
                "option": "Python",
                "isCorrect": false
            },
            {
                "option": "Ruby",
                "isCorrect": false
            },
            {
                "option": "HTML",
                "isCorrect": false
            },
            {
                "option": "CSS 3",
                "isCorrect": false
            },
            {
                "option": "Javascript",
                "isCorrect": false
            },
            {
                "option": "Node",
                "isCorrect": false
            }
        ]
      }'
 ```

- A atividade deve ter uma única alternativa correta.\
  Duas alternativas corretas: Deve retornar HTTP 400 (Single choice task must have exactly one correct option)
```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/singlechoice \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "Duas alternativas corretas",
        "order": 3,
        "options": [
            {
                "option": "Java",
                "isCorrect": true
            },
            {
                "option": "Python",
                "isCorrect": true
            },
            {
                "option": "Ruby",
                "isCorrect": false
            }
        ]
      }'
 ```

- As alternativas devem ter no mínimo 4 e no máximo 80 caracteres.\
  Alternativa com 3 caracteres: Deve retornar HTTP 400 (Option must be between 4 and 80 characters)
```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/singlechoice \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "CSS contem 3 caracteres",
        "order": 3,
        "options": [
            {
                "option": "CSS",
                "isCorrect": true
            },
            {
                "option": "Python",
                "isCorrect": false
            },
            {
                "option": "Ruby",
                "isCorrect": false
            }
        ]
      }'
 ```

- As alternativas não podem ser iguais entre si.\
  Repetição de alternativas, deve retornar 400 (Options cannot be identical)
```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/singlechoice \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "Repete alternativas",
        "order": 3,
        "options": [
            {
                "option": "Python",
                "isCorrect": true
            },
            {
                "option": "Python",
                "isCorrect": false
            },
            {
                "option": "Ruby",
                "isCorrect": false
            }
        ]
      }'
 ```

- As alternativas não podem ser iguais ao enunciado da atividade.
  Repetição de enunciado com uma das alternativas, deve retornar HTTP 400 (Options cannot be identical to the task statement)
```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/singlechoice \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "Enunciado",
        "order": 3,
        "options": [
            {
                "option": "Enunciado",
                "isCorrect": true
            },
            {
                "option": "Python",
                "isCorrect": false
            },
            {
                "option": "Ruby",
                "isCorrect": false
            }
        ]
      }'
 ```

#### Atividade de múltipla escolha

- A atividade deve ter no minimo 3 e no máximo 5 alternativas.
```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/multiplechoice \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "O que aprendemos na aula hoje?",
        "order": 4,
        "options": [
            {
                "option": "Java",
                "isCorrect": true
            },
            {
                "option": "Spring",
                "isCorrect": true
            },
            {
                "option": "Ruby",
                "isCorrect": false
            }
        ]
      }'
 ```

- Excedendo os limites de alternativas: Deve retornar HTTP 400 (Multiple choice task must have between 3 and 5 options)
```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/multiplechoice \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "Número abaixo de alternativas",
        "order": 4,
        "options": [
            {
                "option": "Java",
                "isCorrect": true
            },
            {
                "option": "Ruby",
                "isCorrect": false
            }
        ]
      }'
 ```

```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/multiplechoice \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "Número acima de alternativas",
        "order": 4,
        "options": [
            {
                "option": "Java",
                "isCorrect": true
            },
            {
                "option": "Spring",
                "isCorrect": true
            },
            {
                "option": "Ruby",
                "isCorrect": false
            },
            {
                "option": "HTML",
                "isCorrect": false
            },
            {
                "option": "CSS 3",
                "isCorrect": false
            },
            {
                "option": "Javascript",
                "isCorrect": false
            },
            {
                "option": "Node",
                "isCorrect": false
            }
        ]
      }'
 ```

- A atividade deve ter duas ou mais alternativas corretas, e ao menos uma alternativa incorreta.\
  Dado uma única alternatica correta: Deve retornar HTTP 400 (Multiple choice task must have at least one correct option)
```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/multiplechoice \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "Uma única alternativa correta",
        "order": 4,
        "options": [
            {
                "option": "Java",
                "isCorrect": true
            },
            {
                "option": "Spring",
                "isCorrect": false
            },
            {
                "option": "Ruby",
                "isCorrect": false
            },
            {
                "option": "HTML",
                "isCorrect": false
            }
        ]
      }'
 ```

- As alternativas devem ter no mínimo 4 e no máximo 80 caracteres. (Validado em testes anteriores)
- As alternativas não podem ser iguais entre si.(Validado em testes anteriores)
- As alternativas não podem ser iguais ao enunciado da atividade.(Validado em testes anteriores)

#### Validação de sequência:\n

A ordem das tasks atuais (1, 2, 3, 4):
```bash
curl -w "%{http_code}\n" -X GET http://localhost:8080/tasks
```
Retornará os seguintes registros:
```json
[
  {
    "id": 1,
    "courseId": 1,
    "statement": "What did we learn in today's class?",
    "order": 1,
    "type": "OPEN_TEXT",
    "createdAt": "2025-09-25T15:47:18.409285",
    "options": null
  },
  {
    "id": 2,
    "courseId": 1,
    "statement": "Open-ended Activity",
    "order": 2,
    "type": "OPEN_TEXT",
    "createdAt": "2025-09-25T15:47:38.169104",
    "options": null
  },
  {
    "id": 3,
    "courseId": 1,
    "statement": "Single-choice Activity",
    "order": 3,
    "type": "SINGLE_CHOICE",
    "createdAt": "2025-09-25T15:47:41.906392",
    "options": [
      {
        "id": 1,
        "option": "Java",
        "isCorrect": true
      },
      {
        "id": 2,
        "option": "Python",
        "isCorrect": false
      },
      {
        "id": 3,
        "option": "Ruby",
        "isCorrect": false
      }
    ]
  },
  {
    "id": 4,
    "courseId": 1,
    "statement": "What did we learn in class today?",
    "order": 4,
    "type": "MULTIPLE_CHOICE",
    "createdAt": "2025-09-25T15:48:30.385026",
    "options": [
      {
        "id": 4,
        "option": "Java",
        "isCorrect": true
      },
      {
        "id": 5,
        "option": "Spring",
        "isCorrect": true
      },
      {
        "id": 6,
        "option": "Ruby",
        "isCorrect": false
      }
    ]
  }
]
```

Ao executar
```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/task/new/singlechoice \
  -H "Content-Type: application/json" \
  -d '{
        "courseId": 1,
        "statement": "Task na segunda posição",
        "order": 2,
        "options": [
            {
                "option": "Inteligência Artificial",
                "isCorrect": true
            },
            {
                "option": "Python",
                "isCorrect": false
            },
            {
                "option": "Ruby",
                "isCorrect": false
            }
        ]
      }'
 ```

Resultará em
```json
[
  {
    "id": 1,
    "courseId": 1,
    "statement": "O que aprendemos na aula de hoje?",
    "order": 1,
    "type": "OPEN_TEXT",
    "createdAt": "2025-09-25T15:47:18.409285",
    "options": null
  },
  {
    "id": 2,
    "courseId": 1,
    "statement": "Atividade de Resposta Aberta",
    "order": 3,
    "type": "OPEN_TEXT",
    "createdAt": "2025-09-25T15:47:38.169104",
    "options": null
  },
  {
    "id": 3,
    "courseId": 1,
    "statement": "Atividade de alternativa única",
    "order": 4,
    "type": "SINGLE_CHOICE",
    "createdAt": "2025-09-25T15:47:41.906392",
    "options": [
      {
        "id": 1,
        "option": "Java",
        "isCorrect": true
      },
      {
        "id": 2,
        "option": "Python",
        "isCorrect": false
      },
      {
        "id": 3,
        "option": "Ruby",
        "isCorrect": false
      }
    ]
  },
  {
    "id": 4,
    "courseId": 1,
    "statement": "O que aprendemos na aula hoje?",
    "order": 5,
    "type": "MULTIPLE_CHOICE",
    "createdAt": "2025-09-25T15:48:30.385026",
    "options": [
      {
        "id": 4,
        "option": "Java",
        "isCorrect": true
      },
      {
        "id": 5,
        "option": "Spring",
        "isCorrect": true
      },
      {
        "id": 6,
        "option": "Ruby",
        "isCorrect": false
      }
    ]
  },
  {
    "id": 5,
    "courseId": 1,
    "statement": "Task na segunda posição",
    "order": 2,
    "type": "SINGLE_CHOICE",
    "createdAt": "2025-09-25T16:05:17.007975",
    "options": [
      {
        "id": 7,
        "option": "Inteligência Artificial",
        "isCorrect": true
      },
      {
        "id": 8,
        "option": "Python",
        "isCorrect": false
      },
      {
        "id": 9,
        "option": "Ruby",
        "isCorrect": false
      }
    ]
  }
]
```

### Publicação de Cursos

```bash
curl -w "%{http_code}\n" -X POST http://localhost:8080/course/1/publish
```
Resulta em
```json
{
  "id": 1,
  "title": "Java",
  "status": "PUBLISHED",
  "publishedAt": "2025-09-25T16:26:39.069914191"
}
```

### Relatório de Cursos por Instrutor

- Receber o id do instrutor como parâmetro. Retornar a lista de cursos criados por este instrutor e quantidade de atividades do curso.
```bash
curl -w "%{http_code}\n" -X GET http://localhost:8080/instructor/2/courses
```

Resulta em
```json
{
  "courses": [
    {
      "id": 1,
      "title": "Java",
      "status": "BUILDING",
      "publishedAt": null,
      "taskCount": 0
    }
  ],
  "totalPublishedCourses": 0
}
```

- Caso o usuário não exista, retorna 404.
```bash
curl -w "%{http_code}\n" -X GET http://localhost:8080/instructor/9/courses
```

- Se o usuário existir mas não for instrutor, retorna 400.
```bash
curl -w "%{http_code}\n" -X GET http://localhost:8080/instructor/1/courses
```