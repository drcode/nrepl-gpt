## nRepl/Cider middleware that lets you query chatgpt for clojure-specific questions from the the emacs repl

This library is just a proof of concept, shared in case other people want to do something similar and need a reference project. It does work, but I can't provide assistance with installation (I barely understand cider/nrepl/clojure/emacs interop myself) and can't fix any bug you may encounter. Also, be aware that some of your repl entries will now be sent to a third party, with the associated security risks.

This project requires you to have an OpenAI API key (see https://openai.com/blog/openai-api)

## What it looks like

```
user> (+ 1 2)
3

user> what are legal chars in a keyword

In Clojure, a keyword is a symbol that starts with a colon (":").
The legal characters in a keyword are letters, digits, underscores, 
hyphens, and colons. However, colons are only allowed as the first 
character of the keyword. Here are some examples of valid Clojure 
keywords:

`:foo`
`:abc_def`
`:123`
`:hello-world`
`:user/name`
```

## Installation

If you want to use this in your project, put something like the following in your `deps.edn` file:

```clojure
{:deps    {io.github.drcode/nrepl-gpt {:git/sha "1c4924348473a1c3e5f27fa2849ba66ab925ab5e"}}
 :aliases {:cider-clj {:extra-deps {cider/cider-nrepl {:mvn/version "0.27.2"}}
                       :main-opts  ["-m" "nrepl.cmdline" "--middleware" "[cider.nrepl/cider-middleware,nrepl-gpt.nrepl-gpt/wrap-nrepl-gpt]"]}}}
```

Then put this is your `.emacs` file to trigger the alias:

```clojure
(setq cider-clojure-cli-global-options "-A:cider-clj")
```

The middleware assumes there is a file one level up from your project directory (which is likely your home directory) named `gpt-config.edn` which should look like this:

```clojure
{:openai-api-key "OPEN_AI_KEY"
 :system-prompt  "You perform tasks related to the clojure programming language"}
```

The `system-prompt` tells chatgpt what its "system role" is- With the suggested setting above, it will mainly answer clojure questions. Replace with an empty string if you, instead, want it to answer general questions. (Obviously, you'll want to triple-check that this config file is never stored in a public place, or else people can steal your OpenAI API key.)

Now you should be able to launch emacs, run `cider-jack-in` on your project, and then have access to chatgpt in your repl.

## Usage

nRepl/Cider support multiple clojure expressions to be entered into the repl at once. For my usage (and likely yours) I would only ever pass in a single clojure expression in the repl.

The nrepl-gpt middleware takes advantage of this to forward all repl entries that contain more than one expression (i.e. that "look like a sentence") to chatgpt and then prints the response in the repl.