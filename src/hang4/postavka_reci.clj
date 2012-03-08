(ns hang4.postavka-reci
  (:require [clojure.java.jdbc :as sql]))

;- DEFINISANJE VREDNOSTI ZA POVEZIVANJE SA BAZOM
(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "postgresql"
         :subname "//localhost:5432/hangman" 
         :user "postgres"
         :password "brandonheat"})

;- VRACA FILMOVE IZ BAZE SMESTA IH U VEKTOR I UZIMA JEDAN RANDOM FILM           user=> (get-random-word)
(defn get-all-movies []
  (sql/with-connection db
    (sql/with-query-results res
      ["SELECT naziv FROM filmovi"]
      (into [] res))))
;- END

(defn get-random-word []
  (let  [filmovi (get-all-movies)  random (rand-int (.length filmovi))]
     (:naziv (nth filmovi random))))
;- END


;- U ODNOSU NA SLUCAJNO IZVUCENU REC, UPISUJE ODREDJENI BROJ CRTICA UMESTO SLOVA     user=> (get-unknown-word (get-random-word))
(defn get-unknown-word [film]
   (apply str (vec (repeat (.length film) \-))))
;- END


(defn vrati-lokacije-slova-u-reci [slovo rec]
  "vraca listu sa lokacijama pojavljivanja trazenog slova u reci,
   u slucaju da nema tog slova vraca praznu listu..."
  (map first 
           (filter #(= (second %) slovo)
                   (map-indexed vector rec))))
;- END

;(vrati-lokacije-slova-u-reci \w "titanik")

;-- upisuje dato slovo na nadjene pozicije u nepoznatu rec          user=>(upisi-slova-u-rec \a '(1 5 8) "---------------")
(defn upisi-slova-u-rec 
  "(fn SLOVO LISTA-LOKACIJA REC-U-KOJU-SE-UPISUJU-SLOVA)"
  [slovo lista-pozicija  nepoznata-rec]
  (let [nepoznata (vec nepoznata-rec)]    
    (loop [s slovo lista lista-pozicija nepoznata nepoznata]        
      (if  (nil? (first lista))
               (apply str nepoznata)
               (recur s (rest lista) (assoc nepoznata (first lista) s))
               ))))    
;- END

