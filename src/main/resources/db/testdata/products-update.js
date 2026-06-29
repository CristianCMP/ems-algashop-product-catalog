db.products.aggregate(
[
  {
    $lookup: {
      from: "categories",
      localField: "categoryId",
      foreignField: "_id",
      as: "category_temp"
    }
  },
  {
    $unwind: {
      path: "$category_temp",
      preserveNullAndEmptyArrays: true
    }
  },
  {
    $set: {
      category: {
        _id: "$category_temp._id",
        name: "$category_temp.name",
        enabled: "$category_temp.enabled"
      }
    }
  },
  {
    $project: {
      category_temp: 0
    }
  },
  {
    $merge: {
      into: "products",
      on: "_id",
      whenMatched: "merge"
    }
  }
]
);