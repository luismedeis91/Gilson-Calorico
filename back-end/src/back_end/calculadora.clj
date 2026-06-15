(ns back-end.calculadora
  (:import [java.time LocalDate]
           [java.time.format DateTimeFormatter]))

(def formato-data-br (DateTimeFormatter/ofPattern "dd/MM/yyyy"))

(defn parse-data [data]
  (when data
    (LocalDate/parse data formato-data-br)))

(defn transacao-no-periodo? [transacao data-inicial data-final]
  (let [data (parse-data (:data transacao))
        inicio (when data-inicial (parse-data data-inicial))
        fim (when data-final (parse-data data-final))]
    (and data
         (or (nil? inicio) (not (.isBefore data inicio)))
         (or (nil? fim) (not (.isAfter data fim))))))

(defn filtrar-transacoes-por-periodo [transacoes data-inicial data-final]
  (if (and (nil? data-inicial) (nil? data-final))
    transacoes
    (filter #(transacao-no-periodo? % data-inicial data-final) transacoes)))

(defn calcular-saldo [transacoes]
  (reduce (fn [acc transacao]
            (if (= (:tipo transacao) "ganho")
              (+ acc (:calorias transacao))
              (- acc (:calorias transacao))))
          0
          transacoes))
