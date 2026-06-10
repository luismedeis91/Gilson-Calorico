(ns back-end.handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [cheshire.core :as json]
            [back-end.db :as db]
            ))


(defn como-json [conteudo]
{:headers {"Content-Type" "application/json; charset=utf-8"}
:body (json/generate-string conteudo)})

(defroutes app-routes
  (GET "/" []
    "Hello World")

  (GET "/saldo" []
    (como-json {:saldo 20}))
  
  (GET "/calorias" [] (como-json {:transacao (db/transacao_calorias)}))

  (POST "adicionar_calorias" requi (db/adicionar_caloria (:body requi)))
  

  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

