(ns hang4.singleplayer
    (:use compojure.core)
  (:use ring.adapter.jetty,ring.middleware.params, ring.middleware.session,ring.middleware.file-info,ring.middleware.file)
  (:use hiccup.core,hiccup.form-helpers,hiccup.page-helpers)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.java.jdbc :as sql]))

(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "postgresql"
         :subname "//localhost:5432/hangman" 
         :user "postgres"
         :password "brandonheat"})


;- PROVERAVA DA LI JE NEKA IGRA VEC OTVORENA ZA DATOG IGRACA. AKO JESTE VRACA ID_GAME U SUPROTNOM VRACA NIL
(defn open-single-game 
  [user_id]
  (sql/with-connection db 
    (sql/with-query-results res 
      [(str "SELECT * FROM singleplayer WHERE igra_se = 1 AND id_user = ?") user_id]
      (let [red (first res)]
        (:id_game red)))))


(defn vrati-nepoznatu-rec [game_id]
  (sql/with-connection db
    (sql/with-query-results res
      [(str "SELECT nepoznata_rec 
             FROM singleplayer
             WHERE id_game = ?") game_id]
      (let [red (first res)]
        (:nepoznata_rec red)))))


(defn vrati-broj-promasaja [game_id]
 (sql/with-connection db
    (sql/with-query-results res
      [(str "SELECT broj_promasaja 
             FROM singleplayer
             WHERE id_game = ?") game_id]
      (let [red (first res)]
        (:broj_promasaja red)))))


(defn vrati-birana-slova [game_id]
 (sql/with-connection db
    (sql/with-query-results res
      [(str "SELECT birana_slova 
             FROM singleplayer
             WHERE id_game = ?") game_id]
      (let [red (first res)]
        (:birana_slova red)))))


(defn single-page 
  ([session  game_id]
  (let [game_id (if (nil? (:game_id session)) game_id (:game_id session))]
    (html
		    [:html
		     [:head
		      [:title "Hangman - Singleplayer game"]
          (include-js "/jquery.js")
			    (include-js "/jquery.form.js")
          (include-js "/jquery-single-game.js")
              (include-js "/javascript.js")
          (include-css "style.css")		    
		    ]
		     [:body
		      [:div {:id "naslov"} [:h1 "Hangman - Singleplayer game"]
		       [:h3 "Ulogovani ste kao "(:user session)", sa ID-jem "(:user_id session)".. ID_IGRE: "game_id]
		       [:a {:href "/menu"} "back to menu"][:span "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"]
             [:a {:href "#" :class "refresh"} "Refresh."]]
		      [:div {:id "omca"} [:img {:src "pocetna.png"}]]    
		      
		      [:div {:id "slova"} [:h3 "Slova"]
		       [:div {:style "background-image: url(images/let-a.gif)"}  [:a {:class "let" :href "a"} [:img {:src "images/let-a.gif"}]]]
		       [:div {:style "background-image: url(images/let-b.gif)"}  [:a {:class "let" :href "b"} [:img {:src "images/let-b.gif"}]]]
		       [:div {:style "background-image: url(images/let-c.gif)"}  [:a {:class "let" :href "c"} [:img {:src "images/let-c.gif"}]]]
		       
		       [:div {:style "background-image: url(images/let-d.gif)"}  [:a {:class "let" :href "d"} [:img {:src "images/let-d.gif"}]]]
		       [:div {:style "background-image: url(images/let-e.gif)"}  [:a {:class "let" :href "e"} [:img {:src "images/let-e.gif"}]]]
		       [:div {:style "background-image: url(images/let-f.gif)"}  [:a {:class "let" :href "f"} [:img {:src "images/let-f.gif"}]]]
		              
		       [:div {:style "background-image: url(images/let-g.gif)"}  [:a {:class "let" :href "g"} [:img {:src "images/let-g.gif"}]]]
		       [:div {:style "background-image: url(images/let-h.gif)"}  [:a {:class "let" :href "h"} [:img {:src "images/let-h.gif"}]]]
		       [:div {:style "background-image: url(images/let-i.gif)"}  [:a {:class "let" :href "i"} [:img {:src "images/let-i.gif"}]]]
		       
		       [:div {:style "background-image: url(images/let-j.gif)"}  [:a {:class "let" :href "j"} [:img {:src "images/let-j.gif"}]]]
		       [:div {:style "background-image: url(images/let-k.gif)"}  [:a {:class "let" :href "k"} [:img {:src "images/let-k.gif"}]]]
		       [:div {:style "background-image: url(images/let-l.gif)"}  [:a {:class "let" :href "l"} [:img {:src "images/let-l.gif"}]]]
		       
		       [:div {:style "background-image: url(images/let-m.gif)"}  [:a {:class "let" :href "m"} [:img {:src "images/let-m.gif"}]]]
		       [:div {:style "background-image: url(images/let-n.gif)"}  [:a {:class "let" :href "n"} [:img {:src "images/let-n.gif"}]]]
		       [:div {:style "background-image: url(images/let-o.gif)"}  [:a {:class "let" :href "o"} [:img {:src "images/let-o.gif"}]]]
		       
		       [:div {:style "background-image: url(images/let-p.gif)"}  [:a {:class "let" :href "p"} [:img {:src "images/let-p.gif"}]]]
		       [:div {:style "background-image: url(images/let-q.gif)"}  [:a {:class "let" :href "q"} [:img {:src "images/let-q.gif"}]]]
		       [:div {:style "background-image: url(images/let-r.gif)"}  [:a {:class "let" :href "r"} [:img {:src "images/let-r.gif"}]]]
		       [:div {:style "background-image: url(images/let-s.gif)"}  [:a {:class "let" :href "s"} [:img {:src "images/let-s.gif"}]]]
		       
		       [:div {:style "background-image: url(images/let-t.gif)"}  [:a {:class "let" :href "t"} [:img {:src "images/let-t.gif"}]]]
		       [:div {:style "background-image: url(images/let-u.gif)"}  [:a {:class "let" :href "u"} [:img {:src "images/let-u.gif"}]]]
		       [:div {:style "background-image: url(images/let-v.gif)"}  [:a {:class "let" :href "v"} [:img {:src "images/let-v.gif"}]]]
		       
		       [:div {:style "background-image: url(images/let-w.gif)"}  [:a {:class "let" :href "w"} [:img {:src "images/let-w.gif"}]]]
		       [:div {:style "background-image: url(images/let-x.gif)"}  [:a {:class "let" :href "x"} [:img {:src "images/let-x.gif"}]]]
		       [:div {:style "background-image: url(images/let-y.gif)"}  [:a {:class "let" :href "y"} [:img {:src "images/let-y.gif"}]]]
		       [:div {:style "background-image: url(images/let-z.gif)"}  [:a {:class "let" :href "z"} [:img {:src "images/let-z.gif"}]]]
		      ]
          [:div "User id: "[:span {:id "user_id" :class "nepoznata-hidden"} (:user_id session)]]
          [:div "Nepoznata rec: "[:span {:id "nepoznata-rec"  :class "nepoznata-hidden"} (vrati-nepoznatu-rec game_id)]]
          [:div "Broj promasaja: "[:span {:id "broj_promasaja" :class "nepoznata-hidden"} (vrati-broj-promasaja game_id)]]
		      [:div "Birana slova: "[:span {:id "birana_slova"   :class "nepoznata-hidden"} (vrati-birana-slova game_id)]]
	        [:div "Igra se: "[:span {:id "igra_se"   :class "nepoznata-hidden"}]]
          [:div "Pobedio: "[:span {:id "pobedio"   :class "nepoznata-hidden"}]]          
          [:div {:id "nepoznato"}		             
		       ]]]
   )))
  ([session]
  (let [game_id  (:game_id session)]    
    (html
		    [:html
		     [:head
		      [:title "Hangman - Singleplayer game"]
          (include-js "/jquery.js")
			    (include-js "/jquery.form.js")
          (include-js "/jquery-single-game.js")
              (include-js "/javascript.js")
          (include-css "style.css")		    
		    ]
		     [:body
		      [:div {:id "naslov"} [:h1 "Hangman - Singleplayer game"]
		       [:h3 "Ulogovani ste kao "(:user session)", sa ID-jem "(:user_id session)".. ID_IGRE: "game_id]
		       [:a {:href "/game"} "back to menu"][:span "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"]
           [:a {:href "#" :class "refresh"} "Refresh."]]
		       [:div {:id "omca"} [:img {:src "pocetna.png"}]]    
		      
		       [:div {:id "slova"} [:h3 "Slova"]
		       [:div {:style "background-image: url(images/let-a.gif)"}  [:a {:class "let" :href "a"} [:img {:src "images/let-a.gif"}]]]
		       [:div {:style "background-image: url(images/let-b.gif)"}  [:a {:class "let" :href "b"} [:img {:src "images/let-b.gif"}]]]
		       [:div {:style "background-image: url(images/let-c.gif)"}  [:a {:class "let" :href "c"} [:img {:src "images/let-c.gif"}]]]
		       
		       [:div {:style "background-image: url(images/let-d.gif)"}  [:a {:class "let" :href "d"} [:img {:src "images/let-d.gif"}]]]
		       [:div {:style "background-image: url(images/let-e.gif)"}  [:a {:class "let" :href "e"} [:img {:src "images/let-e.gif"}]]]
		       [:div {:style "background-image: url(images/let-f.gif)"}  [:a {:class "let" :href "f"} [:img {:src "images/let-f.gif"}]]]
		              
		       [:div {:style "background-image: url(images/let-g.gif)"}  [:a {:class "let" :href "g"} [:img {:src "images/let-g.gif"}]]]
		       [:div {:style "background-image: url(images/let-h.gif)"}  [:a {:class "let" :href "h"} [:img {:src "images/let-h.gif"}]]]
		       [:div {:style "background-image: url(images/let-i.gif)"}  [:a {:class "let" :href "i"} [:img {:src "images/let-i.gif"}]]]
		       
		       [:div {:style "background-image: url(images/let-j.gif)"}  [:a {:class "let" :href "j"} [:img {:src "images/let-j.gif"}]]]
		       [:div {:style "background-image: url(images/let-k.gif)"}  [:a {:class "let" :href "k"} [:img {:src "images/let-k.gif"}]]]
		       [:div {:style "background-image: url(images/let-l.gif)"}  [:a {:class "let" :href "l"} [:img {:src "images/let-l.gif"}]]]
		       
		       [:div {:style "background-image: url(images/let-m.gif)"}  [:a {:class "let" :href "m"} [:img {:src "images/let-m.gif"}]]]
		       [:div {:style "background-image: url(images/let-n.gif)"}  [:a {:class "let" :href "n"} [:img {:src "images/let-n.gif"}]]]
		       [:div {:style "background-image: url(images/let-o.gif)"}  [:a {:class "let" :href "o"} [:img {:src "images/let-o.gif"}]]]
		       
		       [:div {:style "background-image: url(images/let-p.gif)"}  [:a {:class "let" :href "p"} [:img {:src "images/let-p.gif"}]]]
		       [:div {:style "background-image: url(images/let-q.gif)"}  [:a {:class "let" :href "q"} [:img {:src "images/let-q.gif"}]]]
		       [:div {:style "background-image: url(images/let-r.gif)"}  [:a {:class "let" :href "r"} [:img {:src "images/let-r.gif"}]]]
		       [:div {:style "background-image: url(images/let-s.gif)"}  [:a {:class "let" :href "s"} [:img {:src "images/let-s.gif"}]]]
		       
		       [:div {:style "background-image: url(images/let-t.gif)"}  [:a {:class "let" :href "t"} [:img {:src "images/let-t.gif"}]]]
		       [:div {:style "background-image: url(images/let-u.gif)"}  [:a {:class "let" :href "u"} [:img {:src "images/let-u.gif"}]]]
		       [:div {:style "background-image: url(images/let-v.gif)"}  [:a {:class "let" :href "v"} [:img {:src "images/let-v.gif"}]]]
		       
		       [:div {:style "background-image: url(images/let-w.gif)"}  [:a {:class "let" :href "w"} [:img {:src "images/let-w.gif"}]]]
		       [:div {:style "background-image: url(images/let-x.gif)"}  [:a {:class "let" :href "x"} [:img {:src "images/let-x.gif"}]]]
		       [:div {:style "background-image: url(images/let-y.gif)"}  [:a {:class "let" :href "y"} [:img {:src "images/let-y.gif"}]]]
		       [:div {:style "background-image: url(images/let-z.gif)"}  [:a {:class "let" :href "z"} [:img {:src "images/let-z.gif"}]]]
		      ]
          [:div "Nepoznata rec: "[:span {:id "nepoznata-rec"  :class "nepoznata-hidden"} (vrati-nepoznatu-rec game_id)]]
          [:div "Broj promasaja: "[:span {:id "broj_promasaja" :class "nepoznata-hidden"} (vrati-broj-promasaja game_id)]]
		      [:div "Birana slova: "[:span {:id "birana_slova"   :class "nepoznata-hidden"} (vrati-birana-slova game_id)]]
	        [:div "Igra se: "[:span {:id "igra_se"   :class "nepoznata-hidden"}]]
          [:div "Pobedio: "[:span {:id "pobedio"   :class "nepoznata-hidden"}]]
          [:div {:id "nepoznato"}		             
		       ]]]
   ))))
