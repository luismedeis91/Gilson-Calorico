(ns back-end.db)

(def registro_calorias (atom []))

(defn transacao_calorias []
  @registro_calorias
  )

(defn adicionar_caloria [valor]
  (let [colecao_atualizada(swap! registro_calorias conj valor)]
    (merge valor {:id (count colecao_atualizada)})
    )
  )