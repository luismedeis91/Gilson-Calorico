(ns front-end.core 
  (:gen-class)
  (:require [clj-http.client :as http]
 [cheshire.core :as json]
   )
  
  )

(def chave "hpb0g0UgQHbkfkkxfASQE3rJbRlhnSpiafX6NCNH")


(defn calorias_alementos [alimento]
  (http/get (str "https://api.api-ninjas.com/v1/nutrition")
            {:query-params {"query" alimento}
             :headers {"X-Api-Key" chave}
             })
  )

(defn consulta_calorias []
  (:body (http/get (str "http://localhost:3000/calorias")))
  )


(defn adicionar_consumo_alimento [nome calorias]
  (http/post (str "http://localhost:3000/adicionar_calorias")
             {:content-type :json
              :body (json/generate-string {:nome nome
                                           :tipo "alimento"
                                           :calorias calorias})}))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Escolha uma opcao")
  (println "--------------------------------")
  (println "1 - Cadastrar/consultar dados pessoais")
  (println "2 - Registrar consumo de alimento ")
  (println "3 - Registrar realização de atividade física")
  (println "4 - Consultar extrato de transações")
  (println "5 - Consultar saldo de calorias")
  (let [escolha (read) ]
    (cond 
      (= escolha 1)(do (println "Digite o que você deseja consultar"))
      (= escolha 2) (do
  (println "Digite o alimento que você deseja registrar")
  (let [alimento (read-line)
        resposta (:body (calorias_alementos alimento))
        dados (json/parse-string resposta true)
        calorias (get-in dados ["calories"])]
    (println resposta)
    (println dados)
    (println calorias)))
      (= escolha 3)(do (println "Digite a atividade fisíca que você deseja registrar"))
      (= escolha 4)(do (println "Consulta de saldo de restrições"))
      (= escolha 5)(do (println (consulta_calorias)))


      
      )
    
    )
  
  )
