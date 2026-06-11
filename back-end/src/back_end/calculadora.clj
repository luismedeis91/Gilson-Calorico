(ns back-end.calculadora)

(defn calcular-saldo [transacoes]
  (reduce (fn [acc transacao]
            (if (= (:tipo transacao) "ganho")
              (+ acc (:calorias transacao))
              (- acc (:calorias transacao))))
          0
          transacoes))