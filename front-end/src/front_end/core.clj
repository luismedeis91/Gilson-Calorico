(ns front-end.core 
  (:gen-class)
  (:require [clj-http.client :as http]
 [cheshire.core :as json]
   )
  
  )

(def chave "fn_9mC2Bv4kFnEn0ePOGDlohi4cEj4_-YTLdhZLKCd7W0g")


(defn calorias_alimentos [alimento]
  (try (let [response (http/get "https://calorieapiadmin.com/api/v1/search/foods"
            {:query-params {"q" alimento}
             :headers {"X-Api-Key" chave}
             :as :string})]
  (json/parse-string (:body response) true))
  (catch Exception e (println "Erro ao conectar na API: " (.getMessage e)) nil)))

(defn buscar_exercicio [atividade]
  (try
    (let [response (http/get "https://wger.de/api/v2/exerciseinfo/"
                             {:query-params {"language" 2
                                             "limit" 5
                                             "name" atividade}
                              :as :string})]
      (json/parse-string (:body response) true))
    (catch Exception e
      (println "Erro ao conectar na API de exercicios:" (.getMessage e))
      nil)))

(defn consulta_calorias []
  (:body (http/get (str "http://localhost:3000/calorias")))
  )

(defn consulta_saldo []
  (:body (http/get "http://localhost:3000/saldo"))
  )

(defn adicionar_consumo_alimento [nome calorias]
  (http/post (str "http://localhost:3000/adicionar_calorias")
             {:content-type :json
              :body (json/generate-string {:nome nome
                                           :tipo "ganho"
                                           :calorias calorias})}))

(defn registrar_atividade [nome calorias]
  (http/post "http://localhost:3000/adicionar_calorias"
             {:content-type :json
              :body (json/generate-string {:atividade nome
                                           :tipo "gasto"
                                           :calorias calorias})}))

(defn consultar_dados_pessoais []
  (:body (http/get "http://localhost:3000/usuario")))

(defn consultar_dados_pessoais-json []
  (try
    (json/parse-string (consultar_dados_pessoais) true)
    (catch Exception _ {})))

(defn registrar_dados_pessoais [peso altura idade sexo]
  (http/post "http://localhost:3000/usuario"
             {:content-type :json
              :body (json/generate-string {:peso peso
                                           :altura altura
                                           :idade idade
                                           :sexo sexo})}))

(def met-por-categoria
  {"Cardio" 8.0
   "Legs" 6.0
   "Arms" 4.5
   "Shoulders" 4.5
   "Back" 5.5
   "Chest" 5.0
   "Abs" 4.0
   "Calves" 4.5
   "Full body" 6.5})

(defn primeiro-exercicio [resposta]
  (let [exercicio (first (:results resposta))
        traducao (first (:translations exercicio))]
    (when traducao
      {:nome (:name traducao)
       :categoria (get-in exercicio [:category :name])})))

(defn estimar_calorias_exercicio [peso minutos categoria]
  (let [met (get met-por-categoria categoria 5.0)]
    (/ (* met peso 3.5 minutos) 200.0)))

(defn executar-menu []
  (println "--------------------------------")
  (println "CALCULADORA DE CALORIAS")
  (println "Escolha uma opcao")
  (println "--------------------------------")
  (println "1 - Cadastrar/consultar dados pessoais")
  (println "2 - Registrar consumo de alimento ")
  (println "3 - Registrar realizacao de atividade física")
  (println "4 - Consultar extrato de transacoes")
  (println "5 - Consultar saldo de calorias")
  (println "0 - Sair do Sistema")
  
  (let [entrada (read-line)
        escolha (try (Integer/parseInt entrada) (catch Exception e -1))]
    (cond 
      (= escolha 1)(do (println "Digite o que voce deseja consultar (1 - Consultar, 2 - Cadastrar)" ) 
      (let [opcao-user (read-line)]
      (cond 
      (= opcao-user "1") (println "Dados atuais:" (consultar_dados_pessoais))
      (= opcao-user "2") (do 
        (println "Digite seu peso (kg):")
        (let [peso (read-line)]
          (println "Digite sua altura (cm):")
          (let [altura (read-line)]
            (println "Digite sua idade:")
            (let [idade (read-line)]
              (println "Digite seu sexo (M/F):")
              (let [sexo (read-line)]
                (registrar_dados_pessoais peso altura idade sexo)
                (println "Dados registrados com sucesso.")))))) 
            :else (println "Opcao invalida."))))

      (= escolha 2) (do (println "Digite o alimento que voce deseja registrar")
  (let [alimento (read-line)
        resposta (calorias_alimentos alimento)
        dados (or (:data resposta) (:foods resposta)) 
        primeiro-item (first dados)
        nome-item (:name primeiro-item)
        calorias (or (:calories_100g primeiro-item) (:calories primeiro-item))]
        (if calorias
            (do
              (println (str "Calorias do alimento " nome-item ": " calorias " calorias encontradas."))
              (adicionar_consumo_alimento nome-item calorias)
              (println "Alimento registrado com sucesso."))
            (println "Alimento nao encontrado na base."))
    ))

      (= escolha 3)(do (println "Digite a atividade fisíca que voce deseja registrar")
      (let [atividade (read-line)
            resposta (buscar_exercicio atividade)
            exercicio (primeiro-exercicio resposta)
            nome-atividade (or (:nome exercicio) atividade)
            categoria (:categoria exercicio)
            dados-pessoais (consultar_dados_pessoais-json)
            peso-usuario (try (Double/parseDouble (str (:peso dados-pessoais)))
                              (catch Exception _ nil))]
          (if exercicio
            (println (str "Exercicio encontrado na API: " nome-atividade
                          (when categoria (str " (" categoria ")"))))
            (println "Nenhum exercicio encontrado na API. Usando o nome digitado."))

          (if peso-usuario
            (do
              (println "Quantos minutos voce praticou essa atividade?")
              (let [minutos-str (read-line)
                    minutos (try (Double/parseDouble minutos-str) (catch Exception _ 0))
                    calorias-num (estimar_calorias_exercicio peso-usuario minutos categoria)]
                (registrar_atividade nome-atividade calorias-num)
                (println (str "Atividade registrada com sucesso. Gasto estimado: "
                              (format "%.2f" calorias-num) " calorias."))))
            (do
              (println "Peso do usuario nao cadastrado. Digite quantas calorias voce gastou:")
              (let [calorias-str (read-line)
                    calorias-num (try (Double/parseDouble calorias-str) (catch Exception e 0))]
                (registrar_atividade nome-atividade calorias-num)
                (println "Atividade registrada com sucesso."))))))

      (= escolha 4)(do (println "Consulta de extrato de transacoes")
      (println (consulta_calorias)))

      (= escolha 5)(do (println "Consulta de extrato de transacoes")
      (println (consulta_saldo)))

      (= escolha 0)(println "Encerrando a Calculadora.")

      :else 
      (println "Opcao invalida, digite novamente."))

      (when (not= escolha 0)(recur))
    
    )
  
  )

(defn -main [& args] (executar-menu))
