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

(defroutes app-routes
  (GET "/" []
    "Hello World")

  (GET "/saldo" []
  (let [todas-transacoes (db/transacao_calorias)
          saldo-atual (calc/calcular-saldo todas-transacoes)]
    (como-json {:saldo saldo-atual})))
  
  (GET "/calorias" [] (como-json {:transacao (db/transacao_calorias)}))

  (POST "/adicionar_calorias" requi 
    (let [corpo-texto (slurp (:body requi))
          dados-convertidos (json/parse-string corpo-texto true)
          resultado (db/adicionar_caloria dados-convertidos)]
      (como-json resultado)))

  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes api-defaults))

(GET "/usuario" [] 
    (como-json (db/consultar_usuario)))

(POST "/usuario" requi 
  (let [corpo-texto (slurp (:body requi))
        dados-convertidos (json/parse-string corpo-texto true)
        resultado (db/registrar_usuario dados-convertidos)]
    (como-json resultado)))