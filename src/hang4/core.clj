(ns hang4.core 
  (:use compojure.core)
  (:use ring.adapter.jetty,ring.middleware.params, ring.middleware.session,ring.middleware.file-info,ring.middleware.file)
  (:use hiccup.core,hiccup.form-helpers,hiccup.page-helpers)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.java.jdbc :as sql]
            [hang4.login :as login]
            [hang4.game :as game]
            [hang4.postavka-reci :as pg]
            [hang4.singleplayer :as singlegame]
            [hang4.multiplayer :as multiplayer]))

(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "postgresql"
         :subname "//localhost:5432/hangman" 
         :user "postgres"
         :password "brandonheat"})

;------------------------------------------------------------------------------------
;---------------------------- MULTIPLAYER LOGIC -------------------------------------
(defn update-ajax [user_id]
  (login/update-logovan user_id))

(defn update-ajax-players [user_id]
  (do
    (login/update-logovan user_id)
    (let  [players (multiplayer/get-logged-players user_id)]
      (if (nil? players)
      (html
        "jok...")
      (html
        (multiplayer/write-online-players players)))
      )))

;-- pravljenje poziva za multiplayer igru
(defn call-for-multi-game [player_id_1 player_id_2] 
  (do
     (multiplayer/delete-old-multi player_id_1 player_id_2)
     (game/db-make-new-multi-game player_id_1 (Integer/parseInt player_id_2))
     (let [id_game (game/last-id)]
       (multiplayer/create-call-for-multi id_game player_id_1 (Integer/parseInt player_id_2)))       
    (str "korisnik je pozvan")))

 
(defn call-check [session]
  (let [user (session :user)
        user_id (session :user_id)     
        pozvan (multiplayer/get-multi-calls user_id)
        prihvacen (multiplayer/get-multi-responce user_id)]
     (if (not (nil? pozvan))
       (str "pozvan_od:"(pozvan :id_player_1)",id_call:"(pozvan :id_call)",id_game:"(pozvan :id_game))
       (if (not (nil? prihvacen))
         (str "prihvacen_od:"(prihvacen :id_player_1)",id_call:"(prihvacen :id_call)",id_game:"(prihvacen :id_game))
         (str "nema")
         ))))

;- prihvatanje multiplayer igde
(defn multigame-accepted [user_id id_call]
  (multiplayer/accept-multi-calls id_call))

;- odbijanje multiplayer igde
(defn multigame-declined [user_id id_call]
  (multiplayer/delete-old-multi id_call))



;- ide na logiku igre i proverava da li ima trazenog slova
(defn play-multigame [session slovo id]
  (game/play-multigame session slovo id))
     
;-update-uje status igre i proverava da li joj je kraj
(defn update_multi_game [session id]
  (let [partija (multiplayer/get-multiplayer-game (Integer/parseInt id))]
     (do
		    (if (and (or (= (partija :kraj_1) 0)(= (partija :kraj_1) 1)) (or (= (partija :kraj_2) 0)(= (partija :kraj_2) 1))) 
		       (if (= (partija :igra_se) 1)
			       (sql/with-connection db
			         (sql/do-commands (str "UPDATE multiplayer
			                                SET  igra_se = 0
			                                WHERE id_game = "(partija :id_game))))))
       
         (let [partija (multiplayer/get-multiplayer-game (Integer/parseInt id))]
           (game/write-partija partija (session :user_id)))  
      )))


;------------------------------------------------------------------------------------
;--------------------------- SINGLEPLAYER LOGIC -------------------------------------
;- ucitava single game
(defn single-game [session]
    (let [user_id  (:user_id session)
          game_id  (singlegame/open-single-game  user_id)]   
      (if (nil? game_id)
	      (do
          (game/db-make-new-single-game session)
			    (let [game_id (game/last-single-id)]   
			      {:session {:game_id game_id :user (:user session) :user_id (:user_id session)}
			       :body (singlegame/single-page session)}))
       ;else        
          {:session {:game_id game_id :user (:user session) :user_id (:user_id session)}
			     :body (singlegame/single-page session game_id)}
       )          
     ))

;--obradjuje singlegame partiju
(defn obradi-single-game [slovo session]
  (game/obradi-single-game slovo session)  
  )

;------------------------------------------------------
;---------------------   ROUTES   ---------------------
;------------------------------------------------------
(defroutes main-routes
  (GET  "/" [] (login/login-page))  
  (GET  "/login" [] (login/login-page))
  (POST "/login" {params :params session :session} (login/login-srcipt params session))
  (GET  "/logout" [] (login/logout))
  (GET "/menu" {session :session} (login/menu-page session))
  
  ;- ajax log refresh
  (POST "/ajax-log-refresh" {session :session} (update-ajax (session :user_id)))
  (POST "/ajax-log-refresh-players" {session :session} (update-ajax-players (session :user_id)))
  
  ;- multiplayer section
  (GET "/players" {session :session} (multiplayer/get-multiplayer session))
  (POST "/ajax-zovi-igraca" {session :session params :params}  (call-for-multi-game (session  :user_id) (params :id)))
  (POST "/ajax-call-check"  {session :session} (call-check  session))
  
  ;- prihvatanje ili odbijanje poziva za igru
  (POST "/ajax-prihvatam-igru" {session :session params :params} (multigame-accepted (session :user_id) (params :id_call)))
  (POST "/ajax-odbijam-igru" {session :session params :params}   (multigame-declined (session :user_id) (params :id_call)))
  
  ;- strana multiplayer
  (GET "/multiplayer/:id" {session :session params :params} (multiplayer/multi-page session (params :id)))
  (POST "/ajax-multiplayer-game" {session :session params :params} (play-multigame session (params :slovo) (params :id)))
  (POST "/update_multi_game" {session :session params :params} (update_multi_game session (params :id)))

  ;-- SINGLEPLAYER
  (GET "/singlegame" {session :session} (single-game session))
  (POST "/ajax-single-game" {session :session params :params} (obradi-single-game (params :slovo) session))    
  (route/resources "/")
  (route/not-found "<h2>404 - Page not found..</h2>"))


;-- SETINGS
(wrap! main-routes :session)

(def apps (->
           #'main-routes
           (wrap-file "files")
           (wrap-file "img")
            wrap-file-info))

(def app
  (handler/site apps))