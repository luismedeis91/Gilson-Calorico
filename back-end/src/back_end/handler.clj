(ns back-end.handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [cheshire.core :as json]
            [back-end.db :as db]
            [back-end.calculadora :as calc] 
            ))


(defn como-json [conteudo]
  {:status 200
  :headers {"Content-Type" "application/json; charset=utf-8"}
  :body (json/generate-string conteudo)})

(defn periodo-da-requisicao [requisicao]
  {:inicio (get-in requisicao [:query-params "inicio"])
   :fim (get-in requisicao [:query-params "fim"])})

(defroutes app-routes
  (GET "/" []
    "Hello World")

  (GET "/saldo" requisicao
    (let [todas-transacoes (db/transacao_calorias)
          {:keys [inicio fim]} (periodo-da-requisicao requisicao)
          transacoes-filtradas (calc/filtrar-transacoes-por-periodo todas-transacoes inicio fim)
          saldo-atual (calc/calcular-saldo transacoes-filtradas)]
      (como-json {:saldo saldo-atual
                  :inicio inicio
                  :fim fim})))
  
  (GET "/calorias" requisicao
    (let [todas-transacoes (db/transacao_calorias)
          {:keys [inicio fim]} (periodo-da-requisicao requisicao)
          transacoes-filtradas (calc/filtrar-transacoes-por-periodo todas-transacoes inicio fim)]
      (como-json {:transacao transacoes-filtradas
                  :inicio inicio
                  :fim fim})))

  (POST "/adicionar_calorias" requi 
    (let [corpo-texto (slurp (:body requi))
          dados-convertidos (json/parse-string corpo-texto true)
          resultado (db/adicionar_caloria dados-convertidos)]
      (como-json resultado)))

  (GET "/usuario" [] 
    (como-json (db/consultar_usuario)))

  (POST "/usuario" requi 
    (let [corpo-texto (slurp (:body requi))
          dados-convertidos (json/parse-string corpo-texto true)
          resultado (db/registrar_usuario dados-convertidos)]
      (como-json resultado)))

  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes api-defaults))
