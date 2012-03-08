(ns hang4.login
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

;- PROVERA DA LI POSTOJI KORISNIK ZA DATU KOMBINACIJU USER-PASS               user=> (db-user-check {"user" "pass"}) 
(defn db-user-check
  "Proverava da li ima trazenog korisnika u bazi"
  [params]
		(let [user (params :user) pass (params :pass)]
		  (sql/with-connection db
		    (sql/with-query-results res
		      [(str "SELECT * FROM users WHERE username = ? AND password = ?") user pass]    
				     (let [res (first res)]
                (if (nil?  res)
						       nil
                   {:user user :user_id (res :id)}))))))


;- OSVEZAVA VREME LOGOVANJA
(defn update-logovan [user_id]
  (sql/with-connection db
    (sql/do-commands
         (str "UPDATE users
                    SET logovan = CURRENT_TIMESTAMP
                WHERE id = " user_id))))


;- LOGIN PAGE
(defn login-page []
  (html
    [:hmtl
     [:head
      [:title "Hangman - Login Page"]
      (include-js "/jquery.js")
	    (include-js "/jquery.form.js")
	    (include-css "style.css")]
     [:body
      [:div {:id "hang-bg"}
       [:div {:id "login-form"}
        (form-to {:id "myform"} [:post "/login"]
          [:label [:b [:u "Login form:"]]]
          [:label "Username:"]
		      (text-field :user "janko")
          [:label "Password:"]
		      (text-field :pass "janko1")
		      (submit-button "Login"))
         ;[:a {:href "/register"} "Create user"]
        ]]]]))



;- LOGIN ERROR
(defn not-login []
  (html (include-css "style.css")	
           [:div {:id "no-user" } 
           [:h1 "Greska!!"]
           [:h3 "Niste ulogovani da bi ste videli ovu stranicu!"]
           [:a {:href "/login"} "Back to login page."]]))



;- LOGOUT
(defn logout []
  {:body (html [:html
                [:head
                 (include-css "style.css")]
                [:body
			            [:div {:id "byby-user" }
			            [:h1 "Bye Bye!!"]
			            [:a {:href "/"} "Idi na logovanje"]]]])
   :session nil})



;- LOGIN CHECK
(defn login-srcipt [params session] 
  (let [korisnik (db-user-check params)]    	   			      
     (if (nil? korisnik)
     (html  (include-js "/jquery.js")
			      (include-js "/jquery.form.js")
			      (include-css "style.css")	
           [:div {:id "no-user" } 
           [:h1 "Greska!!"]
           [:h3 "Nije pronadjen korisnik sa datim podacima!"]
           [:a {:href "/login"} "Back to login page."]])
     {:body (html (include-js "/jquery.js")
							    (include-js "/jquery.form.js")
							    (include-css "style.css")
            [:div {:id "found-user" }
            [:h2 "Dobrodosli  "(korisnik :user)"!"]
            [:a {:href "/menu"} "udji u igru"]])
      :session {:user (korisnik :user) 
                :user_id (korisnik :user_id)}})))



;- MENU PAGE
(defn menu-page [session]
  (if (nil? (:user session))
    (not-login)
    (do      
      (html
      [:hmtl
	     [:head
	      [:title "Hangman - Menu Page"]
	      (include-js "/jquery.js")
		    (include-js "/jquery.form.js")
		    (include-css "style.css")
        (include-js "/javascript.js")]
	     [:body
	      [:div {:id "naslov"} 
           [:h1 "HANGMAN - MENU"]
           [:span "Ulogovani ste kao "[:b (:user session)"("(:user_id session)")"]]]
        [:div {:style "position: relative"}
	        [:div {:id "menu-bg"} 
	         [:div {:id "menu"}
	          [:a {:href "singlegame" :class "menu-item" :id "menu_up"}   "singleplayer" ]
	          [:a {:href "players"    :class "menu-item" :id "menu_midd"} "multiplayer" ]
	          [:a {:href "logout"     :class "menu-item" :id "menu_down"} "exit" ]
	          ]
	         ]
	         [:div {:id "menu_bg1" :class "menu-bg"} ]
	         [:div {:id "menu_bg2" :class "menu-bg"} ]
	         [:div {:id "menu_bg3" :class "menu-bg"} ]
         ]]]))))
;- END

;-- vraca podatke za korisnika
(defn get-user [user_id]
  (sql/with-connection db
    (sql/with-query-results res
      [(str "SELECT * FROM users WHERE id = ?") user_id]
      (first res))))
      







