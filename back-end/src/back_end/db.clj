(ns back-end.db)

(def registro_calorias (atom []))

(defn transacao_calorias []
  @registro_calorias
  )

(defn adicionar_caloria [valor]
  (let [id-novo (count @registro_calorias)
        valor-com-id (assoc valor :id id-novo)]
    (swap! registro_calorias conj valor-com-id)
    valor-com-id)
  )
  