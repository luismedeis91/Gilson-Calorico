(ns front-end.core 
  (:gen-class)
  (:require [clj-http.client :as http]
 [cheshire.core :as json]
   )
  
  )

(def chave "hpb0g0UgQHbkfkkxfASQE3rJbRlhnSpiafX6NCNH")


(defn calorias_alimentos [alimento]
  (http/get (str "https://api.api-ninjas.com/v1/nutrition")
            {:query-params {"query" alimento}
             :headers {"X-Api-Key" chave}
             })
  )

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

(defn executar-menu []
  (println "--------------------------------")
  (println "CALCULADORA DE CALORIAS")
  (println "Escolha uma opcao")
  (println "--------------------------------")
  (println "1 - Cadastrar/consultar dados pessoais")
  (println "2 - Registrar consumo de alimento ")
  (println "3 - Registrar realização de atividade física")
  (println "4 - Consultar extrato de transações")
  (println "5 - Consultar saldo de calorias")
  (println "0 - Sair do Sistema")
  
  (let [entrada (read-line)
        escolha (try (Integer/parseInt entrada) (catch Exception e -1))]
    (cond 
      (= escolha 1)(do (println "Digite o que você deseja consultar"))

      (= escolha 2) (do (println "Digite o alimento que você deseja registrar")
  (let [alimento (read-line)
        resposta (try (:body (calorias_alimentos alimento)) (catch Exception e "[]"))
        dados (json/parse-string resposta true)
        primeiro-item (first dados)
        calorias (get-in dados ["calories"])]
        (if calorias
            (do
              (println (str "Calorias: " calorias " calorias encontradas."))
              (adicionar_consumo_alimento alimento calorias)
              (println "Alimento registrado com sucesso."))
            (println "Alimento não encontrado na base."))
    (println resposta)
    (println dados)
    (println calorias)))

      (= escolha 3)(do (println "Digite a atividade fisíca que você deseja registrar")
      (let [atividade (read-line)]
          (println "Quantas calorias você gastou?")
          (let [calorias-str (read-line)
                calorias-num (try (Double/parseDouble calorias-str) (catch Exception e 0))]
            (registrar_atividade atividade calorias-num)
            (println "Atividade registrada com sucesso."))))

      (= escolha 4)(do (println "Consulta de extrato de transações")
      (println (consulta_calorias)))

      (= escolha 5)(do (println "Consulta de extrato de transações")
      (println (consulta_saldo)))

      (= escolha 0)(println "Encerrando a Calculadora.")

      :else 
      (println "Opção inválida, digite novamente."))

      (when (not= escolha 0)(recur))
    
    )
  
  )

(defn -main [& args] (executar-menu))
