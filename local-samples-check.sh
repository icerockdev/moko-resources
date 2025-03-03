#
# Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
#

set -e

(cd samples/android-mpp-app && ./local-check.sh)
(cd samples/auto-manifest && ./local-check.sh)
(cd samples/compose-jvm-app && ./local-check.sh)
(cd samples/ios-static-xcframework && ./local-check.sh)
(cd samples/ios-cocoapods-static-framework && ./local-check.sh)
(cd samples/kotlin-ios-app && ./local-check.sh)
(cd samples/resources-gallery && ./local-check.sh)
(cd samples/compose-resources-gallery && ./local-check.sh)
(cd samples/default-hierarchy-gallery-mobile && ./local-check.sh)
(cd samples/kotlin-2-sample && ./local-check.sh)
(cd samples/kotlin-2-dynamic-sample && ./local-check.sh)
(cd samples/kotlin-2-tests && ./local-check.sh)
(cd samples/cm-resources-sample && ./local-check.sh)
