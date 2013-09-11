PROJECT_ID=9e8a06a43416f7e6e0eb0a4803559e95afc022f5
PROJECT_PATH=$PWD/..
TARGET=$PWD/target

rm -r $TARGET
mkdir $TARGET
cd $TARGET

wget --no-check-certificate https://webtranslateit.com/api/projects/$PROJECT_ID/zip_file -O i18n.zip
unzip i18n.zip
for STRING_FILE in `ls buddycloud-android*`; do
  STRING_LOCALE=`echo $STRING_FILE | cut -d'.' -f2`
  if [ "$STRING_LOCALE" != "xml" ]; then
    LOCALE_LA=`echo $STRING_LOCALE | cut -d'-' -f1`
    LOCALE_RE=`echo $STRING_LOCALE | cut -d'-' -f2`
    if [ "$LOCALE_LA" == "$LOCALE_RE" ]; then
      VALUES_DIR=$PROJECT_PATH/res/values-$LOCALE_LA
    else
      VALUES_DIR=$PROJECT_PATH/res/values-$LOCALE_LA-r$LOCALE_RE
    fi
    mkdir -p $VALUES_DIR
    cp $STRING_FILE $VALUES_DIR/strings.xml
  fi
done

cd $PROJECT_PATH
rm -r $TARGET