(ns hang4.game
  (:require [clojure.java.jdbc :as sql]
            [hang4.multiplayer :as multiplayer]
            [hang4.postavka-reci :as pg]))


;- DEFINISANJE VREDNOSTI ZA POVEZIVANJE SA BAZOM
(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "postgresql"
         :subname "//localhost:5432/hangman" 
         :user "postgres"
         :password "brandonheat"})



;- POSTAVLJANJE NOVE multiplayer igre u bazi               user=> (db-make-new-single-game user_1 user_2)
(defn db-make-new-multi-game [user_id_1 user_id_2]
  (let [data-rec-1 (pg/get-random-word)         
        nepoznata-rec-1 (pg/upisi-slova-u-rec \space (pg/vrati-lokacije-slova-u-reci \space data-rec-1) (pg/get-unknown-word data-rec-1))
        data-rec-2 (pg/get-random-word)         
        nepoznata-rec-2 (pg/upisi-slova-u-rec \space (pg/vrati-lokacije-slova-u-reci \space data-rec-2) (pg/get-unknown-word data-rec-2))
        ]    
		(sql/with-connection db		
      (sql/insert-records :multiplayer {   :igra_se 1
                                           :pobedio_igrac 0
                                           
                                           :id_player_1      user_id_1
                                           :data_rec_1       data-rec-1
                                           :nepoznata_rec_1  nepoznata-rec-1
                                           :birana_slova_1   ""
                                           :broj_promasaja_1 0                                          
                                           
                                           :id_player_2      user_id_2
                                           :data_rec_2       data-rec-2
                                           :nepoznata_rec_2  nepoznata-rec-2
                                           :birana_slova_2   ""
                                           :broj_promasaja_2 0                                           
                                        })     
     )))
;end

;- vraca id poslednje napravljene multiplayer igre
(defn last-id [] 
  (sql/with-connection db
    (sql/with-query-results res 
      ["SELECT MAX(id_game) as maxid FROM multiplayer"]
      (let [red (first res)]
        (red :maxid))
    )))


(defn write-partija [partija user_id]
  (str "user_id:"user_id";"
           "igra_se:"(partija :igra_se)";"
           "pobedio_igrac:"(partija :pobedio_igrac)";"           
           "id_player_1:"(partija :id_player_1)";"
           "data_rec_1:"(partija :data_rec_1)";"
           "nepoznata_rec_1:"(partija :nepoznata_rec_1)";"
           "birana_slova_1:"(partija :birana_slova_1)";"
           "broj_promasaja_1:"(partija :broj_promasaja_1)";"
           "kraj_1:"(partija :kraj_1)";"           
           "id_player_2:"(partija :id_player_2)";"
           "data_rec_2:"(partija :data_rec_2)";"
           "nepoznata_rec_2:"(partija :nepoznata_rec_2)";"
           "birana_slova_2:"(partija :birana_slova_2)";"
           "broj_promasaja_2:"(partija :broj_promasaja_2)";"
           "kraj_2:"(partija :kraj_2))
      )

;;============================================================================================================
;;============================================================================================================

(defn play-multigame [session slovo id]
  (let [user_id (session :user_id)
        slovo (first(vec  slovo))
        partija (multiplayer/get-multiplayer-game (Integer/parseInt id))
        igra_se (partija :igra_se)
        
        id_player_1 (partija :id_player_1)
        data_rec_1  (partija :data_rec_1)
        nepoznata_rec_1 (partija :nepoznata_rec_1)
        birana_slova_1 (partija :birana_slova_1)
        broj_promasaja_1 (partija :broj_promasaja_1)
        kraj_1 (partija :kraj_1)
        
        id_player_2 (partija :id_player_2)
        data_rec_2  (partija :data_rec_2)
        nepoznata_rec_2 (partija :nepoznata_rec_2)
        birana_slova_2 (partija :birana_slova_2)
        broj_promasaja_2 (partija :broj_promasaja_2)
        kraj_2 (partija :kraj_2)
        ]
  (do
    ;----
       (if (= id_player_1 user_id)
         ;-- ako je prvi igrac 
         (if (and (not (= kraj_1 0))(not (= kraj_1 1)))
	         ;- ako nije kraj
           (let [lokacije-slova (pg/vrati-lokacije-slova-u-reci slovo data_rec_1)]      
	           (if (nil? (first lokacije-slova))
	             ;nema slova u reci
	             (if (< broj_promasaja_1 5)
			             ;nema slova u reci
	                 ;(apply str "nema slovo za igraca 1,data_rec:"data_rec_1", slovo:"slovo", lok: "lokacije-slova)
			             (multiplayer/update-multi-nema-slovo partija slovo user_id) 
	                 ;else kraj
	                 ;(str "nema slovo i kraj za igraca 1")
	                 (multiplayer/update-multi-nema-slovo-kraj partija slovo user_id ) 
			          )
	             ;ima slova u reci
	             (let [nova-nepoznata-rec (pg/upisi-slova-u-rec slovo lokacije-slova nepoznata_rec_1)]
	               (if (not(some #{\-} nova-nepoznata-rec))
	                 ;kraj pobeda  
	                 ;(str "ima slovo i kraj za igraca 1")
	                 (multiplayer/update-multi-ima-slovo-kraj partija slovo nova-nepoznata-rec user_id)
	                 ;else nije kraj
	                 ;(str "ima slovo za igraca 1")
	                 (multiplayer/update-multi-ima-slovo partija slovo nova-nepoznata-rec user_id)
	               )
	             )
	           )
	          )
           ;- else kraj
           (str "Kraj. Nepoznata rec je: "data_rec_1)
          )
         
         ;-- ako je drugi igrac 
         (if (and (not (= kraj_2 0))(not (= kraj_2 1)))
	         ;- ako nije kraj         
		         (let [lokacije-slova (pg/vrati-lokacije-slova-u-reci slovo data_rec_2)]
		           (if (nil? (first lokacije-slova))
		             ;nema slova u reci
		              (if (< broj_promasaja_2 5)
				             ;nema slova u reci
		                 ;(str "nema slovo za igraca 2")
				             (multiplayer/update-multi-nema-slovo partija slovo user_id) 
		                 ;else kraj
		                 ;(str "nema slovo i kraj za igraca 2")
		                 (multiplayer/update-multi-nema-slovo-kraj partija slovo user_id) 
				           )
		             ;ima slova u reci
		            (let [nova-nepoznata-rec (pg/upisi-slova-u-rec slovo lokacije-slova nepoznata_rec_2)]
		              (if (not(some #{\-} nova-nepoznata-rec))
		                 ;kraj pobeda  
		                 ;(str "ima slovo i kraj za igraca 2")
		                 (multiplayer/update-multi-ima-slovo-kraj partija slovo nova-nepoznata-rec user_id)
		                 ;else nije kraj
		                 ;(str "ima slovo za igraca 2")
		                 (multiplayer/update-multi-ima-slovo partija slovo nova-nepoznata-rec user_id)
		              )
		             )
		           )
		         )
           
        ;- else kraj
                (str "Kraj. Nepoznata rec je: "data_rec_2)
          )
      )
    ;----
    (let [partija (multiplayer/get-multiplayer-game (Integer/parseInt id))]
      (write-partija partija user_id)
      )      
    )))
;---------- END

;;====================================================================================================
;;===========================     SINGLEGAME     =====================================================
;;====================================================================================================

;- POSTAVLJANJE NOVE SINGLEPLAYER IGRE U BAZI               user=> (db-make-new-single-game 32)
(defn db-make-new-single-game [session]
  (let [user_id (:user_id session)
        data-rec (pg/get-random-word)         
        nepoznata-rec (pg/upisi-slova-u-rec \space (pg/vrati-lokacije-slova-u-reci \space data-rec) (pg/get-unknown-word data-rec))]    
		(sql/with-connection db		 
      (sql/insert-records :singleplayer {  :id_user user_id
		                                       :igra_se 1                               
						                               :broj_promasaja 0
						                               :data_rec data-rec
						                               :nepoznata_rec nepoznata-rec
						                               :birana_slova ""
                                           :pobedio 0
                                        })     
     )))
;end

(defn last-single-id []
  (sql/with-connection db	
    (sql/with-query-results res["SELECT MAX(id_game) AS id FROM singleplayer"]
         (:id (first res)))))
;- END

;;---------------------------------

(defn get-singlegame [game_id]
  (sql/with-connection db
    (sql/with-query-results res
      [(str "SELECT * 
             FROM singleplayer 
             WHERE id_game = ?") game_id]
	     (let [res (first res)]		      
			       {:game_id (:id_game res)
	            :user_id (:id_user res)
	            :igra_se (:igra_se res)
              :broj_promasaja (:broj_promasaja res)
	            :data_rec (:data_rec res)
	            :nepoznata_rec (:nepoznata_rec res)
	            :birana_slova (:birana_slova res)  
              :pobedio (:pobedio res)
            }) 
      )))

(defn update-single-nema-slova [partija slovo]
    (sql/with-connection db
      (sql/update-values :singleplayer ["id_game=?" (partija :game_id)]
                       {:broj_promasaja (+ 1 (partija :broj_promasaja))
                        :birana_slova (str (partija :birana_slova)","slovo)
                        })))

(defn update-single-nema-slova-kraj [partija slovo]
    (sql/with-connection db
      (sql/update-values :singleplayer ["id_game=?" (partija :game_id)]
                       {:broj_promasaja (+ 1 (partija :broj_promasaja))
                        :birana_slova (str (partija :birana_slova)","slovo)
                        :igra_se 0
                        :pobedio 0
                        })
))

(defn update-single-ima-slova-kraj [partija slovo nova-nepoznata-rec]  
    (sql/with-connection db
      (sql/update-values :singleplayer ["id_game=?" (partija :game_id)]
                       {:igra_se 0
                        :birana_slova (str (partija :birana_slova)","slovo)
                        :pobedio 1
                        :nepoznata_rec nova-nepoznata-rec
                        })
))

(defn update-single-ima-slova [partija slovo nova-nepoznata-rec]
    ;ima jos slova za igru
    (sql/with-connection db
      (sql/update-values :singleplayer ["id_game=?" (partija :game_id)]
                       {:birana_slova (str (partija :birana_slova)","slovo)                        
                        :nepoznata_rec nova-nepoznata-rec
                        })
))



(defn obradi-single-game [slovo session]
  (let [game_id (session :game_id)
        user_id (session :user_id)
        slovo (first(vec  slovo))
        partija (get-singlegame game_id)
        lokacije-slova (pg/vrati-lokacije-slova-u-reci slovo (:data_rec partija))        
        igra_se (:igra_se partija)
        broj_promasaja (:broj_promasaja partija)
        nepoznata-rec (:nepoznata_rec partija)
        ]
      (do
        (if (nil? (first lokacije-slova))
         ;-nema slova u reci
         (if (< broj_promasaja 5 )
           ;nije jos kraj
           (update-single-nema-slova partija slovo)
           ;else kraj
           (update-single-nema-slova-kraj partija slovo)
           )
         
         ;ima slova u reci
         (let [nova-nepoznata-rec (pg/upisi-slova-u-rec slovo lokacije-slova nepoznata-rec)]
           (if (not(some #{\-} nova-nepoznata-rec))
             ;-kraj pobeda
             (update-single-ima-slova-kraj partija slovo nova-nepoznata-rec)
             ;nije kraj
             (update-single-ima-slova partija slovo nova-nepoznata-rec)
           )))
        
        (let [partija (get-singlegame game_id)]
          (str "igra_se:"(partija :igra_se)
               ";broj_promasaja:"(partija :broj_promasaja)
               ";nepoznata_rec:"(partija :nepoznata_rec)
               ";birana_slova:"(partija :birana_slova)
               ";pobedio:"(partija :pobedio))            
          )           
        )))










