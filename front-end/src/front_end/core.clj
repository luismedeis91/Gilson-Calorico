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

(defn registrar_dados_pessoais [peso altura idade sexo]
  (http/post "http://localhost:3000/usuario"
             {:content-type :json
              :body (json/generate-string {:peso peso
                                           :altura altura
                                           :idade idade
                                           :sexo sexo})}))

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
      (let [atividade (read-line)]
          (println "Quantas calorias voce gastou?")
          (let [calorias-str (read-line)
                calorias-num (try (Double/parseDouble calorias-str) (catch Exception e 0))]
            (registrar_atividade atividade calorias-num)
            (println "Atividade registrada com sucesso."))))

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
