AREAID ?= :akiku

CMD_LIST := dev run uber sync

all:
	@echo Usage:
	@echo make dev
	@echo make run
	@echo make uber
	@echo make sync

.PHONY: $(CMD_LIST)
.SILENT: $(CMD_LIST)

dev:
	clj -M:dev

run:
	# clj -M -m odekake.core
	java -jar target/odekake.jar $(AREAID)

uber:
	clojure -T:build clean
	clojure -T:build uber

sync:
	java -jar target/odekake.jar :akiku
	java -jar target/odekake.jar :saka
	git com -am "Update weather"
	git push

%:
	@:

