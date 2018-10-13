# ScalaAPI作成
### ScalaだけでCRUD機能作成　→　APIサーバに変更する

## ScalaAPI作成操作メモ(TODOあとでwikiか何かに移す)

作成時のテンプレート
　https://www.playframework.com/getting-started
　sbt new playframework/play-scala-seed.g8

#### 詳細はログ参照

DB設定追加

不要なので下記フォルダ・ファイル等を消す
　*gradle* .g8/ .settings 

データベースとユーザーの作成
sh ./initial_data/create-local-mysql-db.sh

env/dev.conf
flywayLocationNames = ["common", "mysql"]
flywayLocations = ["filesystem:conf/db/migration/default/common", "filesystem:conf/db/migration/default/mysql"]
jdbcDriver = "com.mysql.cj.jdbc.Driver"
jdbcUrl = "jdbc:mysql://localhost:3306/micro_posts?autoReconnect=true&useSSL=false"
jdbcUserName = "micro_posts"
jdbcPassword = "パスワード"
