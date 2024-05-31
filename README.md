# Odekake(おでかけ)

## 機能

- おでかけ前の天気予報チェック
- 天気予報サイトをスクレイピングして、静的ページにまとめる
  - ウェザーニュース
  - tenki.jp
  - Yahoo!
- https://gpsoft.github.io/odekake/

## 開発

```
$ clj -M:dev
$ vim src/odekake/core.clj
  :Connect 5876 src

$ clj -M -m odekake.core
```

## リリース

```
$ clj -T:build clean
$ clj -T:build uber
```

## 使い方

```
$ java -jar odekake.jar --help
or
$ AREAID=:akiku make run
```

## メモ

```
https://weathernews.jp/onebox/tenki/hiroshima/34107/
https://tenki.jp/forecast/7/37/6710/34107/10days.html
https://weather.yahoo.co.jp/weather/jp/34/6710/34107.html
37 広島
35 島根
38 山口
34 広島
32 島根
35 山口
["中区" "hiroshima" 7 37 6710 34101 34]
["東区" "hiroshima" 7 37 6710 34102 34]
["南区" "hiroshima" 7 37 6710 34103 34]
["西区" "hiroshima" 7 37 6710 34104 34]
["安佐南区" "hiroshima" 7 37 6710 34105 34]
["安佐北区" "hiroshima" 7 37 6710 34106 34]
["安芸区" "hiroshima" 7 37 6710 34107 34]
["佐伯区" "hiroshima" 7 37 6710 34108 34]
["呉市" "hiroshima" 7 37 6710 34202 34]
["竹原市" "hiroshima" 7 37 6710 34203 34]
["三原市" "hiroshima" 7 37 6710 34204 34]
["尾道市" "hiroshima" 7 37 6710 34205 34]
["福山市" "hiroshima" 7 37 6710 34207 34]
["府中市" "hiroshima" 7 37 6710 34208 34]
["三次市" "hiroshima" 7 37 6720 34209 34]
["大竹市" "hiroshima" 7 37 6710 34211 34]
["東広島市" "hiroshima" 7 37 6710 34212 34]
["廿日市市" "hiroshima" 7 37 6710 34213 34]
["安芸高田市" "hiroshima" 7 37 6720 34214 34]
["江田島市" "hiroshima" 7 37 6710 34215 34]
["府中町" "hiroshima" 7 37 6710 34302 34]
["海田町" "hiroshima" 7 37 6710 34304 34]
["熊野町" "hiroshima" 7 37 6710 34307 34]
["坂町" "hiroshima" 7 37 6710 34309 34]
["安芸太田町" "hiroshima" 7 37 6720 34368 34]
["北広島町" "hiroshima" 7 37 6720 34369 34]
["大崎上島町" "hiroshima" 7 37 6710 34431 34]
["世羅町" "hiroshima" 7 37 6710 34462 34]
["神石高原町" "hiroshima" 7 37 6710 34545 34]
6810 32201 島根県松江市
6820 32202 島根県浜田市
6810 32203 島根県出雲市
6820 32204 島根県益田市
6820 32205 島根県大田市
6820 32207 島根県江津市
8110 35201 山口県下関市
8110 35202 山口県宇部市
8120 35203 山口県山口市
8120 35206 山口県防府市
8130 35208 山口県岩国市
8120 35215 山口県周南市
8110 35216 山口県山陽小野田市
8130 35305 山口県周防大島町
```
