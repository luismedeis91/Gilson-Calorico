(ns front-end.core 
  (:gen-class)
  (:require [clj-http.client :as http]
 [cheshire.core :as json]
   )
  
  )

(def chave "fn_9mC2Bv4kFnEn0ePOGDlohi4cEj4_-YTLdhZLKCd7W0g")
(def chave-ninjas
  (or (System/getenv "API_NINJAS_KEY")
      (System/getenv "NINJAS_API_KEY")
      "hpb0g0UgQHbkfkkxfASQE3rJbRlhnSpiafX6NCNH"))


(defn calorias_alimentos [alimento]
  (try (let [response (http/get "https://calorieapiadmin.com/api/v1/search/foods"
            {:query-params {"q" alimento}
             :headers {"X-Api-Key" chave}
             :as :string})]
  (json/parse-string (:body response) true))
  (catch Exception e (println "Erro ao conectar na API: " (.getMessage e)) nil)))

(defn buscar_exercicio [atividade minutos peso-libras]
  (try
    (let [duracao-int (max 1 (int minutos))
          params (cond-> {"activity" atividade
                          "duration" duracao-int}
                          peso-libras (assoc "weight" (max 50 (min 500 (int peso-libras)))))
      response (http/get "https://api.api-ninjas.com/v1/caloriesburned"
                             {:query-params params
                              :headers {"X-Api-Key" chave-ninjas}
                              :as :string})]
      (json/parse-string (:body response) true))
    (catch Exception e
      (println "Erro ao conectar na API de exercicios:" (.getMessage e))
      nil)))

(defn consulta_calorias
  ([] (consulta_calorias nil nil))
  ([inicio fim]
   (:body (http/get "http://localhost:3000/calorias"
                    {:query-params (cond-> {}
                                     inicio (assoc "inicio" inicio)
                                     fim (assoc "fim" fim))}))))

(defn consulta_saldo
  ([] (consulta_saldo nil nil))
  ([inicio fim]
   (:body (http/get "http://localhost:3000/saldo"
                    {:query-params (cond-> {}
                                     inicio (assoc "inicio" inicio)
                                     fim (assoc "fim" fim))}))))

(defn adicionar_consumo_alimento [nome calorias quantidade data]
  (http/post (str "http://localhost:3000/adicionar_calorias")
             {:content-type :json
              :body (json/generate-string {:nome nome
                                           :tipo "ganho"
                                           :calorias calorias
                                           :quantidade quantidade
                                           :data data})}))

(defn registrar_atividade [nome calorias duracao data]
  (http/post "http://localhost:3000/adicionar_calorias"
             {:content-type :json
              :body (json/generate-string {:atividade nome
                                           :tipo "gasto"
                                           :calorias calorias
                                           :duracao duracao
                                           :data data})}))

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

(defn opcoes-exercicio [resposta]
  (vec
   (take 5
         (map (fn [exercicio]
                {:nome (:name exercicio)
                 :calorias (:total_calories exercicio)
                 :duracao (:duration_minutes exercicio)})
              resposta))))

(defn escolher-exercicio [opcoes]
  (doseq [[indice exercicio] (map-indexed vector opcoes)]
    (println (str (inc indice) " - " (:nome exercicio)
                  (when (:calorias exercicio)
                    (str " - " (:calorias exercicio) " calorias")))))
  (println "Escolha o numero do exercicio:")
  (let [entrada (read-line)
        escolha (try (Integer/parseInt entrada) (catch Exception _ -1))]
    (get opcoes (dec escolha))))

(defn kg-para-libras [peso-kg]
  (* peso-kg 2.20462))

(defn ler-data []
  (println "Digite a data no formato DD/MM/AAAA:")
  (read-line))

(defn ler-periodo []
  (println "Digite a data inicial no formato DD/MM/AAAA:")
  (let [inicio (read-line)]
    (println "Digite a data final no formato DD/MM/AAAA:")
    (let [fim (read-line)]
      [inicio fim])))

(defn ler-tipo-consulta []
  (println "Digite 1 para consultar tudo ou 2 para consultar por periodo:")
  (read-line))

(defn executar-menu []
  (println "--------------------------------")
  (println "CALCULADORA DE CALORIAS")
  (println "Escolha uma opcao")
  (println "--------------------------------")
  (println "1 - Cadastrar/consultar dados pessoais")
  (println "2 - Registrar consumo de alimento ")
  (println "3 - Registrar realizacao de atividade fisica")
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
        nome-item (or (:name primeiro-item) alimento)
        calorias (or (:calories_100g primeiro-item) (:calories primeiro-item))]
        (if calorias
            (do
              (println (str "Calorias do alimento " nome-item ": " calorias " calorias encontradas."))
              (println "Digite a quantidade consumida em gramas:")
              (let [quantidade-str (read-line)
                    quantidade (try (Double/parseDouble quantidade-str) (catch Exception _ 100.0))
                    data (ler-data)
                    calorias-ajustadas (* calorias (/ quantidade 100.0))]
                (adicionar_consumo_alimento nome-item calorias-ajustadas quantidade data))
              (println "Alimento registrado com sucesso."))
            (do (println "Nenhum alimento encontrado na base. Digite quantas calorias voce consumiu:")
                (let [calorias-str (read-line)
                      calorias-manual (try (Double/parseDouble calorias-str) (catch Exception _ 0))
                      data (ler-data)]
                  (adicionar_consumo_alimento nome-item calorias-manual 1.0 data)
                  (println "Alimento registrado com sucesso."))))
    ))

      (= escolha 3)(do (println "Digite a atividade fisica que voce deseja registrar")
      (let [atividade (read-line)
            dados-pessoais (consultar_dados_pessoais-json)
            peso-usuario (try (Double/parseDouble (str (:peso dados-pessoais)))
                              (catch Exception _ nil))]
          (println "Quantos minutos voce praticou essa atividade?")
          (let [minutos-str (read-line)
                minutos (try (Double/parseDouble minutos-str) (catch Exception _ 0))
                peso-libras (when peso-usuario (kg-para-libras peso-usuario))
                resposta (buscar_exercicio atividade minutos peso-libras)
                opcoes (opcoes-exercicio resposta)
                exercicio (when (seq opcoes) (escolher-exercicio opcoes))
                nome-atividade (or (:nome exercicio) atividade)
                calorias-num (double (or (:calorias exercicio) 0))
                data (ler-data)]
            (if exercicio
              (do
                (registrar_atividade nome-atividade calorias-num minutos data)
                (println (str "Atividade registrada com sucesso. Gasto estimado: "
                              (format "%.2f" calorias-num) " calorias.")))
              (do
                (println "Nenhum exercicio encontrado na API. Digite quantas calorias voce gastou:")
                (let [calorias-str (read-line)
                      calorias-manual (try (Double/parseDouble calorias-str) (catch Exception _ 0))]
                  (registrar_atividade nome-atividade calorias-manual minutos data)
                  (println "Atividade registrada com sucesso.")))))))

      (= escolha 4)(do (println "Consulta de extrato de transacoes")
      (let [tipo-consulta (ler-tipo-consulta)]
        (if (= tipo-consulta "1")
          (println (consulta_calorias))
          (let [[inicio fim] (ler-periodo)]
            (println (consulta_calorias inicio fim))))))

      (= escolha 5)(do (println "Consulta de saldo de calorias")
      (let [tipo-consulta (ler-tipo-consulta)]
        (if (= tipo-consulta "1")
          (println (consulta_saldo))
          (let [[inicio fim] (ler-periodo)]
            (println (consulta_saldo inicio fim))))))

      (= escolha 0)(println "Encerrando a Calculadora.")

      :else 
      (println "Opcao invalida, digite novamente."))

      (when (not= escolha 0)(recur))
    
    )
  
  )

(defn -main [& args] (executar-menu))
