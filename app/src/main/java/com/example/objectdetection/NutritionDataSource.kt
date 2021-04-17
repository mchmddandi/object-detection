package com.example.objectdetection

object NutritionDataSource {
    fun getNutrition(foodName: String): List<NutritionAdapter.Nutrition>{
        return when(foodName){
            "Nasi" -> getNasiNutrition()
            "Telur" -> getTelurNutrition()
            "Anggur" -> getAnggurNutrition()
            "Pisang" -> getPisangNutrition()
            "Brokoli" -> getBrokoliNutrition()
            else -> emptyList()
        }
    }

    private fun getNasiNutrition(): List<NutritionAdapter.Nutrition>{
        return listOf(
                NutritionAdapter.Nutrition(
                        label = "Berat (g)",
                        value = "100"
                ),
                NutritionAdapter.Nutrition(
                        label = "Kalori (kal)",
                        value = "180"
                ),
                NutritionAdapter.Nutrition(
                        label = "Protein (g)",
                        value = "3"
                ),
                NutritionAdapter.Nutrition(
                        label = "Lemak (g)",
                        value = "0.3"
                ),
                NutritionAdapter.Nutrition(
                        label = "Karbohidrat",
                        value = "39.8"
                ),
                NutritionAdapter.Nutrition(
                        label = "Serat",
                        value = "0.2"
                )
        )
    }

    private fun getTelurNutrition(): List<NutritionAdapter.Nutrition>{
        return listOf(
                NutritionAdapter.Nutrition(
                        label = "Berat (g)",
                        value = "100"
                ),
                NutritionAdapter.Nutrition(
                        label = "Kalori (kal)",
                        value = "251"
                ),
                NutritionAdapter.Nutrition(
                        label = "Protein (g)",
                        value = "16.3"
                ),
                NutritionAdapter.Nutrition(
                        label = "Lemak (g)",
                        value = "19.4"
                ),
                NutritionAdapter.Nutrition(
                        label = "Karbohidrat",
                        value = "1.4"
                ),
                NutritionAdapter.Nutrition(
                        label = "Serat",
                        value = "0"
                )
        )
    }

    private fun getAnggurNutrition(): List<NutritionAdapter.Nutrition>{
        return listOf(
                NutritionAdapter.Nutrition(
                        label = "Berat (g)",
                        value = "100"
                ),
                NutritionAdapter.Nutrition(
                        label = "Kalori (kal)",
                        value = "30"
                ),
                NutritionAdapter.Nutrition(
                        label = "Protein (g)",
                        value = "0.5"
                ),
                NutritionAdapter.Nutrition(
                        label = "Lemak (g)",
                        value = "0.2"
                ),
                NutritionAdapter.Nutrition(
                        label = "Karbohidrat",
                        value = "6.8"
                ),
                NutritionAdapter.Nutrition(
                        label = "Serat",
                        value = "1.2"
                )
        )
    }

    private fun getPisangNutrition(): List<NutritionAdapter.Nutrition>{
        return listOf(
                NutritionAdapter.Nutrition(
                        label = "Berat (g)",
                        value = "100"
                ),
                NutritionAdapter.Nutrition(
                        label = "Kalori (kal)",
                        value = "108"
                ),
                NutritionAdapter.Nutrition(
                        label = "Protein (g)",
                        value = "1"
                ),
                NutritionAdapter.Nutrition(
                        label = "Lemak (g)",
                        value = "0.8"
                ),
                NutritionAdapter.Nutrition(
                        label = "Karbohidrat",
                        value = "24.3"
                ),
                NutritionAdapter.Nutrition(
                        label = "Serat",
                        value = "1.9"
                )
        )
    }

    private fun getBrokoliNutrition(): List<NutritionAdapter.Nutrition>{
        return listOf(
                NutritionAdapter.Nutrition(
                        label = "Berat (g)",
                        value = "100"
                ),
                NutritionAdapter.Nutrition(
                        label = "Kalori (kal)",
                        value = "34"
                ),
                NutritionAdapter.Nutrition(
                        label = "Protein (g)",
                        value = "2.8"
                ),
                NutritionAdapter.Nutrition(
                        label = "Lemak (g)",
                        value = "0.3"
                ),
                NutritionAdapter.Nutrition(
                        label = "Karbohidrat",
                        value = "6.6"
                ),
                NutritionAdapter.Nutrition(
                        label = "Serat",
                        value = "2.4"
                )
        )
    }
}