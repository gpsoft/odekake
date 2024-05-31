AREAID ?= :akiku

CMD_LIST := dev run uber

all:
	@echo Usage:
	@echo make dev
	@echo make run
	@echo make uber

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

%:
	@:

