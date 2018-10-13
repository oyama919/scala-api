# ScalaAPI作成
### ScalaだけでCRUD機能作成　→　APIサーバに変更する

## ScalaAPI作成操作メモ(TODOあとでwikiか何かに移す)

作成時のテンプレート
　sbt new playframework/play-scala-seed.g8

#### 詳細はログ参照

DB設定追加

不要なので下記フォルダ・ファイル等を消す
　*gradle* .g8/ .settings 

データベースとユーザーの作成
sh ./initial_data/create-local-mysql-db.sh
