(ns hang4.multiplayer 
  (:use compojure.core)
  (:use ring.adapter.jetty,ring.middleware.params, ring.middleware.session,ring.middleware.file-info,ring.middleware.file)
  (:use hiccup.core,hiccup.form-helpers,hiccup.page-helpers)
  (:require [compojure.route :as route]
            [compojure.handler :as handler] 
            [hang4.login :as login]
            [clojure.java.jdbc :as sql]))

(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "postgresql"
         :subname "//localhost:5432/hangman" 
         :user "postgres"
         :password "brandonheat"})


;- VRACA SVE IGRACE KOJI SU ULOGOVANI ZA POTENCIJALNI MULTIPLAYER
(defn get-logged-players [user_id]
  (sql/with-connection db
    (sql/with-query-results res 
      [ (str "SELECT  id, username
             FROM users
             WHERE logovan + interval '15 seconds' > current_timestamp AND id <> ? ") user_id]         
          (vec res)
        )))

(defn write-online-players [players]
  (let [lista ""]
	             (loop [p players i (count players) lista lista]
	               (if  (< i 1)
	                 [:h2 [:u {:style "color: blue;"} "Aktivni igraci: "] lista]  
	                 (recur p (dec i) (str lista "<h3><a class=\"pozovi_za_multiplayer\" href=\""((get p (- i 1)) :id)"\">"((get p (- i 1)) :username)"</a></h3>") )
	                 )))
  )


(defn get-multiplayer [session]
  (let [user_id (session :user_id) 
        players (get-logged-players user_id)]
    (do
      ;(login/update-logovan user_id)
      (if (nil? (get players 0))
        (html
		      [:hmtl
			     [:head
			      [:title "Hangman - Multiplayer"]
			      (include-js "/jquery.js")
				    (include-js "/jquery.form.js")
            (include-js "/javascript-players.js")
				    (include-css "style.css")]
			     [:body
			      [:div {:id "naslov"} 
		           [:h1 "HANGMAN - MENU"]
		           [:span "Ulogovani ste kao "[:b (:user session)"("(:user_id session)")"]][:br]
             [:a {:href "/menu"} "back to menu"]]              
             [:div {:id "lista-igraca"}
		            [:h3 "Aktivni korisnici:"]]
            ]])
         (html 
           [:html
            [:head
				      [:title "Hangman - Multiplayer"]
				      (include-js "/jquery.js")
					    (include-js "/jquery.form.js")
              (include-js "/javascript-players.js")
					    (include-css "style.css")]
            [:body
             [:div {:id "naslov"} 
		           [:h1 "HANGMAN - MENU"]
		           [:span "Ulogovani ste kao "[:b (:user session)"("(:user_id session)")"]][:br]
             [:a {:href "/menu"} "back to menu"]]  
             [:div {:id "lista-igraca"}
	             (write-online-players players)        
              ]
            
           ]])))))



;============================================ MULTIPLAYER GAME =============================================
;- proverava za korisnika da li ga je neko pozvao na partiju
(defn get-multi-calls [user_id]
  (sql/with-connection db
    (sql/with-query-results res 
      [(str "SELECT * 
             FROM multi_calls 
             WHERE prihvatio_2 = 0 AND
                   igra_se = 0 AND 
                   id_player_2 = ? ") user_id]
      (first res))))

;(get-multi-calls 5)

;- proverava za korisnika da li mu je pozvani igrac prihvatio partiju
(defn get-multi-responce [user_id]
  (sql/with-connection db
    (sql/with-query-results res 
      [(str "SELECT * 
            FROM multi_calls 
            WHERE prihvatio_2 = 1 AND 
                  igra_se = 1 AND 
                  id_player_1 = ? ") user_id]
      (first res))))

;(get-multi-responce 5)


;- brise pri pozivu igraca stari poziv koji je bio upucen njemu
(defn delete-old-multi 
  ([call_id]
	  (sql/with-connection db
	   (sql/do-commands
	         (str "DELETE FROM multi_calls 
	               WHERE id_call = "call_id))))
  ([first_user_id second_user_id]
	  (sql/with-connection db
	   (sql/do-commands
	         (str "DELETE FROM multi_calls 
	               WHERE id_player_1 = "first_user_id" AND id_player_2 = "second_user_id))))
  )



 (defn accept-multi-calls [id_call]
   (sql/with-connection db
     (sql/do-commands (str "UPDATE multi_calls 
                                SET   prihvatio_2 = 1,
                                      igra_se = 1
                            WHERE id_call = "id_call))))
 
 
;- pravi poziv za igru za dva korisnika
(defn create-call-for-multi [id_game first_user_id second_user_id]
	(sql/with-connection db
	  (sql/insert-records :multi_calls  {  :id_player_1  first_user_id; (Integer/parseInt first_user_id)                          
					                               :id_player_2  second_user_id; (Integer/parseInt second_user_id)
					                               :prihvatio_1 1
					                               :prihvatio_2 0
					                               :igra_se 0
                                         :id_game id_game
					                               })))

;=======================================================================================================================
;======================================     LOGIKA MULTI PLAYER IGRE    ================================================

;- VRACA SVE PODATKE O MULTIPLAYER IGRI
(defn get-multiplayer-game [id_game]
  (sql/with-connection db
    (sql/with-query-results res 
      [(str "SELECT * FROM multiplayer WHERE id_game = ?")id_game]
        (first res))))

;- dodaj promasaj igracu sa ovim ID-ijem
(defn update-multi-nema-slovo [partija slovo user_id]
  (if (= (partija :id_player_1) user_id)
    ;-ako je prvi igrac
    (sql/with-connection db
      (sql/update-values :multiplayer ["id_game=?" (partija :id_game)]
								                      {:broj_promasaja_1 (+ 1 (partija :broj_promasaja_1))
								                       :birana_slova_1 (str (partija :birana_slova_1)","slovo)}))								                        
    ;-ako je drugi igrac
    (sql/with-connection db
      (sql/update-values :multiplayer ["id_game=?" (partija :id_game)]
								                       {:broj_promasaja_2 (+ 1 (partija :broj_promasaja_2))
								                        :birana_slova_2 (str (partija :birana_slova_2)","slovo)}))))								                        
;-end


;- dodaj promasaj i stavi da je igrac sa datim ID-ijem izgubio
(defn update-multi-nema-slovo-kraj [partija slovo user_id]
  (if (= (partija :id_player_1) user_id)  
    (if (or (= (partija :kraj_2) 0) (= (partija :kraj_2) 1))
		    (sql/with-connection db
		      (sql/update-values :multiplayer ["id_game=?" (partija :id_game)]
										                       {:broj_promasaja_1 (+ 1 (partija :broj_promasaja_1))
										                        :birana_slova_1 (str (partija :birana_slova_1)","slovo)								                        
										                        :igra_se 0
                                            :kraj_1 0}))
         (sql/with-connection db
		      (sql/update-values :multiplayer ["id_game=?" (partija :id_game)]
										                       {:broj_promasaja_1 (+ 1 (partija :broj_promasaja_1))
										                        :birana_slova_1 (str (partija :birana_slova_1)","slovo)								                        
										                        :kraj_1 0}))
      
    )
    (if (or (= (partija :kraj_2) 0) (= (partija :kraj_2) 1))
			    (sql/with-connection db
			      (sql/update-values :multiplayer ["id_game=?" (partija :id_game)]
											                       {:broj_promasaja_2 (+ 1 (partija :broj_promasaja_2))
											                        :birana_slova_2 (str (partija :birana_slova_2)","slovo)								                        
											                        :igra_se 0
                                               :kraj_2 0}))
           (sql/with-connection db
			      (sql/update-values :multiplayer ["id_game=?" (partija :id_game)]
											                       {:broj_promasaja_2 (+ 1 (partija :broj_promasaja_2))
											                        :birana_slova_2 (str (partija :birana_slova_2)","slovo)								                        
											                        :kraj_2 0})))))
;-end

;- stavi da je korisnik sa datim ID-ijem uspesno zavrsio svoju igru
(defn update-multi-ima-slovo-kraj [partija slovo nova-nepoznata-rec user_id]  
  (if (= (partija :id_player_1) user_id)   
    (if (or (= (partija :kraj_2) 0) (= (partija :kraj_2) 1))
	    (sql/with-connection db
	      (sql/update-values :multiplayer ["id_game=?" (partija :id_game)]
									                       {:birana_slova_1 (str (partija :birana_slova_1)","slovo)
									                        :kraj_1 1
                                          :igra_se 0
									                        :nepoznata_rec_1 nova-nepoznata-rec
									                        }))
      (sql/with-connection db
	      (sql/update-values :multiplayer ["id_game=?" (partija :id_game)]
									                       {:birana_slova_1 (str (partija :birana_slova_1)","slovo)
									                        :kraj_1 1
									                        :nepoznata_rec_1 nova-nepoznata-rec
									                        }))
    )
    (if (or (= (partija :kraj_2) 0) (= (partija :kraj_2) 1))
    (sql/with-connection db
      (sql/update-values :multiplayer ["id_game=?" (partija :id_game)]
								                       {:birana_slova_2 (str (partija :birana_slova_2)","slovo)
								                        :kraj_2 1
                                        :igra_se 0
								                        :nepoznata_rec_2 nova-nepoznata-rec
								                        }))
    (sql/with-connection db
      (sql/update-values :multiplayer ["id_game=?" (partija :id_game)]
								                       {:birana_slova_2 (str (partija :birana_slova_2)","slovo)
								                        :kraj_2 1
								                        :nepoznata_rec_2 nova-nepoznata-rec
								                        }))
    )
    ))
;-end

;- stavi da je korisnik sa datim ID-jem pogodio slovo ali nije zavrsio
(defn update-multi-ima-slovo [partija slovo nova-nepoznata-rec user_id]
  (if (= (partija :id_player_1) user_id) 
    (sql/with-connection db
      (sql/update-values :multiplayer ["id_game=?" (partija :id_game)]
									                     {:birana_slova_1 (str (partija :birana_slova_1)","slovo)                        
									                      :nepoznata_rec_1 nova-nepoznata-rec
									                      }))
    (sql/with-connection db
      (sql/update-values :multiplayer ["id_game=?" (partija :id_game)]
									                     {:birana_slova_2 (str (partija :birana_slova_2)","slovo)                        
									                      :nepoznata_rec_2 nova-nepoznata-rec
									                      }))))
;-end




;=======================================================================================================================
;======================================     PRIKAZ MULTI PLAYER IGRE    ================================================


(defn multi-page [session id_game]
    (let [partija (get-multiplayer-game (Integer/parseInt id_game))]
  (html
		    [:html
		     [:head
		      [:title "Hangman - Singleplayer game"]
          (include-js "/jquery.js")
			    (include-js "/jquery.form.js")
          (include-js "/jquery-multiplayer-game.js")              
          (include-css "/style.css")		  
		     ]
       [:body
        [:div {:id "naslov"} [:h1 "Hangman - Multiplayer game"]		       
		       [:a {:href "/menu"} "back to menu"]]
        [:span {:style "display:none;" :id "id_multiplayer_igre"} (partija :id_game)]
        [:span {:style "display:none;" :id "igra_se"} (partija :igra_se)]
         ;-- tabla 1        
            [:div {:class "tabla"}
             [:h3 "Igrac: "((login/get-user (partija :id_player_1)) :username)]
             [:div {:id "omca_multi_1" :class "omca-multi"} [:img {:src "../pocetna.png"}]]
             (if (= (partija :id_player_1) (session :user_id))
             (html 
	             [:div {:id "slova_multi_1" :class "slova-multi"} 
	                 [:div {:style "background-image: url(../images/let-a.gif)"}  [:a {:class "let-multi-1" :href "a"} [:img {:src "../images/let-a.gif"}]]]
						       [:div {:style "background-image: url(../images/let-b.gif)"}  [:a {:class "let-multi-1" :href "b"} [:img {:src "../images/let-b.gif"}]]]
						       [:div {:style "background-image: url(../images/let-c.gif)"}  [:a {:class "let-multi-1" :href "c"} [:img {:src "../images/let-c.gif"}]]]
						       
						       [:div {:style "background-image: url(../images/let-d.gif)"}  [:a {:class "let-multi-1" :href "d"} [:img {:src "../images/let-d.gif"}]]]
						       [:div {:style "background-image: url(../images/let-e.gif)"}  [:a {:class "let-multi-1" :href "e"} [:img {:src "../images/let-e.gif"}]]]
						       [:div {:style "background-image: url(../images/let-f.gif)"}  [:a {:class "let-multi-1" :href "f"} [:img {:src "../images/let-f.gif"}]]]
						              
						       [:div {:style "background-image: url(../images/let-g.gif)"}  [:a {:class "let-multi-1" :href "g"} [:img {:src "../images/let-g.gif"}]]]
						       [:div {:style "background-image: url(../images/let-h.gif)"}  [:a {:class "let-multi-1" :href "h"} [:img {:src "../images/let-h.gif"}]]]
						       [:div {:style "background-image: url(../images/let-i.gif)"}  [:a {:class "let-multi-1" :href "i"} [:img {:src "../images/let-i.gif"}]]]
						       
						       [:div {:style "background-image: url(../images/let-j.gif)"}  [:a {:class "let-multi-1" :href "j"} [:img {:src "../images/let-j.gif"}]]]
						       [:div {:style "background-image: url(../images/let-k.gif)"}  [:a {:class "let-multi-1" :href "k"} [:img {:src "../images/let-k.gif"}]]]
						       [:div {:style "background-image: url(../images/let-l.gif)"}  [:a {:class "let-multi-1" :href "l"} [:img {:src "../images/let-l.gif"}]]]
						       
						       [:div {:style "background-image: url(../images/let-m.gif)"}  [:a {:class "let-multi-1" :href "m"} [:img {:src "../images/let-m.gif"}]]]
						       [:div {:style "background-image: url(../images/let-n.gif)"}  [:a {:class "let-multi-1" :href "n"} [:img {:src "../images/let-n.gif"}]]]
						       [:div {:style "background-image: url(../images/let-o.gif)"}  [:a {:class "let-multi-1" :href "o"} [:img {:src "../images/let-o.gif"}]]]
						       
						       [:div {:style "background-image: url(../images/let-p.gif)"}  [:a {:class "let-multi-1" :href "p"} [:img {:src "../images/let-p.gif"}]]]
						       [:div {:style "background-image: url(../images/let-q.gif)"}  [:a {:class "let-multi-1" :href "q"} [:img {:src "../images/let-q.gif"}]]]
						       [:div {:style "background-image: url(../images/let-r.gif)"}  [:a {:class "let-multi-1" :href "r"} [:img {:src "../images/let-r.gif"}]]]
						       [:div {:style "background-image: url(../images/let-s.gif)"}  [:a {:class "let-multi-1" :href "s"} [:img {:src "../images/let-s.gif"}]]]
						       
						       [:div {:style "background-image: url(../images/let-t.gif)"}  [:a {:class "let-multi-1" :href "t"} [:img {:src "../images/let-t.gif"}]]]
						       [:div {:style "background-image: url(../images/let-u.gif)"}  [:a {:class "let-multi-1" :href "u"} [:img {:src "../images/let-u.gif"}]]]
						       [:div {:style "background-image: url(../images/let-v.gif)"}  [:a {:class "let-multi-1" :href "v"} [:img {:src "../images/let-v.gif"}]]]
						       
						       [:div {:style "background-image: url(../images/let-w.gif)"}  [:a {:class "let-multi-1" :href "w"} [:img {:src "../images/let-w.gif"}]]]
						       [:div {:style "background-image: url(../images/let-x.gif)"}  [:a {:class "let-multi-1" :href "x"} [:img {:src "../images/let-x.gif"}]]]
						       [:div {:style "background-image: url(../images/let-y.gif)"}  [:a {:class "let-multi-1" :href "y"} [:img {:src "../images/let-y.gif"}]]]
						       [:div {:style "background-image: url(../images/let-z.gif)"}  [:a {:class "let-multi-1" :href "z"} [:img {:src "../images/let-z.gif"}]]]
	              ]
              )
              (html 
	             [:div {:id "slova_multi_1" :class "slova-multi"} 
	                 [:div {:style "background-image: url(../images/let-a.gif)"}]
						       [:div {:style "background-image: url(../images/let-b.gif)"}]
						       [:div {:style "background-image: url(../images/let-c.gif)"}]
						       
						       [:div {:style "background-image: url(../images/let-d.gif)"}]
						       [:div {:style "background-image: url(../images/let-e.gif)"}]
						       [:div {:style "background-image: url(../images/let-f.gif)"}]
						              
						       [:div {:style "background-image: url(../images/let-g.gif)"}]
						       [:div {:style "background-image: url(../images/let-h.gif)"}]
						       [:div {:style "background-image: url(../images/let-i.gif)"}]
						       
						       [:div {:style "background-image: url(../images/let-j.gif)"}]
						       [:div {:style "background-image: url(../images/let-k.gif)"}]
						       [:div {:style "background-image: url(../images/let-l.gif)"}]
						       
						       [:div {:style "background-image: url(../images/let-m.gif)"}]
						       [:div {:style "background-image: url(../images/let-n.gif)"}]
						       [:div {:style "background-image: url(../images/let-o.gif)"}]
						       
						       [:div {:style "background-image: url(../images/let-p.gif)"}]
						       [:div {:style "background-image: url(../images/let-q.gif)"}]
						       [:div {:style "background-image: url(../images/let-r.gif)"}]
						       [:div {:style "background-image: url(../images/let-s.gif)"}]
						       
						       [:div {:style "background-image: url(../images/let-t.gif)"}]
						       [:div {:style "background-image: url(../images/let-u.gif)"}]
						       [:div {:style "background-image: url(../images/let-v.gif)"}]
						      
						       [:div {:style "background-image: url(../images/let-w.gif)"}]
						       [:div {:style "background-image: url(../images/let-x.gif)"}]
						       [:div {:style "background-image: url(../images/let-y.gif)"}]
						       [:div {:style "background-image: url(../images/let-z.gif)"}]
	              ]
              ))
             
             
             
              [:div {:style "height: 200px;"}                
               [:div "ID player:"[:span {:id "user_id1" :class "nepoznata_hidden"}(partija :id_player_1)]]
               [:div "Data rec:" [:span {:id "data_rec1" :class "nepoznata_hidden"}(partija :data_rec_1)]]
               [:div "Nepoznata rec:"[:span {:id "nepoznata1" :class "nepoznata_hidden"}(partija :nepoznata_rec_1)]]
               [:div "Birana slova:"[:span {:id "birana_slova_1" :class "nepoznata_hidden"}(partija :birana_slova_1)]]
               [:div "Broj promasaja:"[:span {:id "broj_promasaja_1" :class "nepoznata_hidden"}(partija :broj_promasaja_1)]]
               ]
              [:div {:id "nepoznato_multi_1"}                
               
              ]
             ]         
         ;-- end tabla 1
         [:div {:style "float: left; background-color: #222; height: 5px; width: 1200px;"}]
         ;-- tabla 2
             
             [:div {:class "tabla"}
             [:h3 "Igrac: "((login/get-user (partija :id_player_2)) :username)]
             [:div {:id "omca_multi_2" :class "omca-multi"} [:img {:src "../pocetna.png"}]]
             (if (= (partija :id_player_2) (session :user_id))
             (html 
	             [:div {:id "slova_multi_2"  :class "slova-multi"} 
	                 [:div {:style "background-image: url(../images/let-a.gif)"}  [:a {:class "let-multi-2" :href "a"} [:img {:src "../images/let-a.gif"}]]]
						       [:div {:style "background-image: url(../images/let-b.gif)"}  [:a {:class "let-multi-2" :href "b"} [:img {:src "../images/let-b.gif"}]]]
						       [:div {:style "background-image: url(../images/let-c.gif)"}  [:a {:class "let-multi-2" :href "c"} [:img {:src "../images/let-c.gif"}]]]
						       
						       [:div {:style "background-image: url(../images/let-d.gif)"}  [:a {:class "let-multi-2" :href "d"} [:img {:src "../images/let-d.gif"}]]]
						       [:div {:style "background-image: url(../images/let-e.gif)"}  [:a {:class "let-multi-2" :href "e"} [:img {:src "../images/let-e.gif"}]]]
						       [:div {:style "background-image: url(../images/let-f.gif)"}  [:a {:class "let-multi-2" :href "f"} [:img {:src "../images/let-f.gif"}]]]
						              
						       [:div {:style "background-image: url(../images/let-g.gif)"}  [:a {:class "let-multi-2" :href "g"} [:img {:src "../images/let-g.gif"}]]]
						       [:div {:style "background-image: url(../images/let-h.gif)"}  [:a {:class "let-multi-2" :href "h"} [:img {:src "../images/let-h.gif"}]]]
						       [:div {:style "background-image: url(../images/let-i.gif)"}  [:a {:class "let-multi-2" :href "i"} [:img {:src "../images/let-i.gif"}]]]
						       
						       [:div {:style "background-image: url(../images/let-j.gif)"}  [:a {:class "let-multi-2" :href "j"} [:img {:src "../images/let-j.gif"}]]]
						       [:div {:style "background-image: url(../images/let-k.gif)"}  [:a {:class "let-multi-2" :href "k"} [:img {:src "../images/let-k.gif"}]]]
						       [:div {:style "background-image: url(../images/let-l.gif)"}  [:a {:class "let-multi-2" :href "l"} [:img {:src "../images/let-l.gif"}]]]
						       
						       [:div {:style "background-image: url(../images/let-m.gif)"}  [:a {:class "let-multi-2" :href "m"} [:img {:src "../images/let-m.gif"}]]]
						       [:div {:style "background-image: url(../images/let-n.gif)"}  [:a {:class "let-multi-2" :href "n"} [:img {:src "../images/let-n.gif"}]]]
						       [:div {:style "background-image: url(../images/let-o.gif)"}  [:a {:class "let-multi-2" :href "o"} [:img {:src "../images/let-o.gif"}]]]
						       
						       [:div {:style "background-image: url(../images/let-p.gif)"}  [:a {:class "let-multi-2" :href "p"} [:img {:src "../images/let-p.gif"}]]]
						       [:div {:style "background-image: url(../images/let-q.gif)"}  [:a {:class "let-multi-2" :href "q"} [:img {:src "../images/let-q.gif"}]]]
						       [:div {:style "background-image: url(../images/let-r.gif)"}  [:a {:class "let-multi-2" :href "r"} [:img {:src "../images/let-r.gif"}]]]
						       [:div {:style "background-image: url(../images/let-s.gif)"}  [:a {:class "let-multi-2" :href "s"} [:img {:src "../images/let-s.gif"}]]]
						       
						       [:div {:style "background-image: url(../images/let-t.gif)"}  [:a {:class "let-multi-2" :href "t"} [:img {:src "../images/let-t.gif"}]]]
						       [:div {:style "background-image: url(../images/let-u.gif)"}  [:a {:class "let-multi-2" :href "u"} [:img {:src "../images/let-u.gif"}]]]
						       [:div {:style "background-image: url(../images/let-v.gif)"}  [:a {:class "let-multi-2" :href "v"} [:img {:src "../images/let-v.gif"}]]]
						       
						       [:div {:style "background-image: url(../images/let-w.gif)"}  [:a {:class "let-multi-2" :href "w"} [:img {:src "../images/let-w.gif"}]]]
						       [:div {:style "background-image: url(../images/let-x.gif)"}  [:a {:class "let-multi-2" :href "x"} [:img {:src "../images/let-x.gif"}]]]
						       [:div {:style "background-image: url(../images/let-y.gif)"}  [:a {:class "let-multi-2" :href "y"} [:img {:src "../images/let-y.gif"}]]]
						       [:div {:style "background-image: url(../images/let-z.gif)"}  [:a {:class "let-multi-2" :href "z"} [:img {:src "../images/let-z.gif"}]]]
	              ]
              )
              (html 
	             [:div {:id "slova_multi_2"  :class "slova-multi"} 
	                 [:div {:style "background-image: url(../images/let-a.gif)"}]
						       [:div {:style "background-image: url(../images/let-b.gif)"}]
						       [:div {:style "background-image: url(../images/let-c.gif)"}]
						       
						       [:div {:style "background-image: url(../images/let-d.gif)"}]
						       [:div {:style "background-image: url(../images/let-e.gif)"}]
						       [:div {:style "background-image: url(../images/let-f.gif)"}]
						              
						       [:div {:style "background-image: url(../images/let-g.gif)"}]
						       [:div {:style "background-image: url(../images/let-h.gif)"}]
						       [:div {:style "background-image: url(../images/let-i.gif)"}]
						       
						       [:div {:style "background-image: url(../images/let-j.gif)"}]
						       [:div {:style "background-image: url(../images/let-k.gif)"}]
						       [:div {:style "background-image: url(../images/let-l.gif)"}]
						       
						       [:div {:style "background-image: url(../images/let-m.gif)"}]
						       [:div {:style "background-image: url(../images/let-n.gif)"}]
						       [:div {:style "background-image: url(../images/let-o.gif)"}]
						       
						       [:div {:style "background-image: url(../images/let-p.gif)"}]
						       [:div {:style "background-image: url(../images/let-q.gif)"}]
						       [:div {:style "background-image: url(../images/let-r.gif)"}]
						       [:div {:style "background-image: url(../images/let-s.gif)"}]
						       
						       [:div {:style "background-image: url(../images/let-t.gif)"}]
						       [:div {:style "background-image: url(../images/let-u.gif)"}]
						       [:div {:style "background-image: url(../images/let-v.gif)"}]
						       
						       [:div {:style "background-image: url(../images/let-w.gif)"}]
						       [:div {:style "background-image: url(../images/let-x.gif)"}]
						       [:div {:style "background-image: url(../images/let-y.gif)"}]
						       [:div {:style "background-image: url(../images/let-z.gif)"}]
	              ]
              ))
              [:div {:style "height: 200px;"}
               [:div "ID player:"[:span {:id "user_id2" :class "nepoznata_hidden"}(partija :id_player_2)]]
               [:div "Data rec:" [:span {:id "data_rec2" :class "nepoznata_hidden"}(partija :data_rec_2)]]
               [:div "Nepoznata rec:"[:span {:id "nepoznata2" :class "nepoznata_hidden"}(partija :nepoznata_rec_2)]]
               [:div "Birana slova:"[:span {:id "birana_slova_2" :class "nepoznata_hidden"}(partija :birana_slova_2)]]
               [:div "Broj promasaja:"[:span {:id "broj_promasaja_2" :class "nepoznata_hidden"}(partija :broj_promasaja_2)]]
              ]
              [:div {:id "nepoznato_multi_2"}                
               
              ]
             ]
            
         ;-- end tabla 2
        ]]
  )))

















