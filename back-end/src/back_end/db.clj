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

(def dados_usuario (atom {}))

(defn consultar_usuario []
  @dados_usuario)

(defn registrar_usuario [dados]
  (reset! dados_usuario dados)
  @dados_usuario)