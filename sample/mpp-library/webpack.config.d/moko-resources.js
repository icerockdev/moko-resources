const path = require('path');

const mokoResourcePath = path.resolve("../../../../sample/mpp-library/build/generated/moko/jsMain/resources")

config.module.rules.push(
    {
        test: /\.(.*)/,
        include: [
            path.resolve(mokoResourcePath)
        ],
        type: 'asset/resource'
    }
);

config.resolve.modules.push(
    path.resolve(mokoResourcePath)
)