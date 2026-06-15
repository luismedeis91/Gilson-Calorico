# Calculadora de Calorias em Clojure
Esse projeto utiliza o Leiningen para construção de código, gerenciamento de dependências e requisições HTTP

As APIs escolhidas para o projeto foram: 

- "BusyBody Calorie API", para os alimentos.
- "Calories Burned API", para os exercicios.

Aviso: O nome do exercicio deve ser colocado em inglês.

## Como executar
Abra o terminal na pasta "back-end" e execute o comando a seguir no terminal:
```
lein ring server-headless 3000
```

Em seguida abra outro terminal na pasta "front-end" e execute o comando:
```
lein run
```
