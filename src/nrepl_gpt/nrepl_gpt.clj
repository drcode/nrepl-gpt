(ns nrepl-gpt.nrepl-gpt
  (:require [nrepl.middleware :refer [set-descriptor!]]
            [nrepl.transport :as transport]
            [nrepl.misc :as nm]
            [clojure.string :as st]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [clojure.data.json :as js]))

(def config (edn/read-string (slurp "../gpt-config.edn")))

(defn shell
  ([s ignore-exit-code]
   (let [{:keys [out exit err] :as result} (sh/sh "bash" "-c" s)]
     (when-not (or ignore-exit-code (zero? exit))
       (throw (Exception. (str "exit failed: " err))))
     out))
  ([s]
   (shell s false)))

(defn chatgpt [prompt]
  (get-in (js/read-str (shell (str "curl https://api.openai.com/v1/chat/completions -H \"Authorization: Bearer " (:openai-api-key config) "\" -H \"Content-Type: application/json\" -d '{\"model\": \"gpt-3.5-turbo\", \"messages\": [{\"role\": \"system\", \"content\":\"" (:system-prompt config) "\"},{\"role\": \"user\", \"content\": " (st/replace (pr-str prompt) "'" "`") "}]}'")) :key-fn keyword) [:choices 0 :message :content]))

(defn single-expr? [s]
  (let [reader (java.io.PushbackReader. (io/reader (.getBytes s)))
        expr1  (edn/read {:eof nil} reader)
        expr2  (edn/read {:eof nil} reader)]
    (and (not (nil? expr1))
         (nil? expr2))))

(defn wrap-nrepl-gpt [handler]
  (fn [msg]
    (let [code (:code msg)]
      (if (and code (not (single-expr? code)))
        (do (transport/send (:transport msg) (nm/response-for msg {:out (chatgpt code)}))
            (transport/send (:transport msg) (nm/response-for msg {:status #{:done}})))
        (handler msg)))))

(set-descriptor! #'wrap-nrepl-gpt
                 {:expects #{"eval" "clone"}})