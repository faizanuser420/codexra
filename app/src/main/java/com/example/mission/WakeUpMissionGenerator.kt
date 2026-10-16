package com.example.mission

import kotlin.random.Random

data class MissionQuestion(
    val id: String,
    val prompt: String,
    val questionType: String, // MATHS, SEQUENCE, MEMORY
    val expectedAnswer: String,
    val hint: String = "",
    val options: List<String> = emptyList() // For multiple choice or memory
)

object WakeUpMissionGenerator {

    fun generateQuestion(
        missionType: String = "MATHS",
        difficulty: String = "MEDIUM",
        excludeIds: Set<String> = emptySet()
    ): MissionQuestion {
        var attempts = 0
        while (attempts < 20) {
            val q = when (missionType.uppercase()) {
                "SEQUENCE" -> generateSequenceQuestion(difficulty)
                "MEMORY" -> generateMemoryQuestion(difficulty)
                else -> generateMathQuestion(difficulty)
            }
            if (q.id !in excludeIds) {
                return q
            }
            attempts++
        }
        return generateMathQuestion(difficulty)
    }

    private fun generateMathQuestion(difficulty: String): MissionQuestion {
        val random = Random.Default
        return when (difficulty.uppercase()) {
            "EASY" -> {
                // Two numbers addition or subtraction up to 50
                val op = if (random.nextBoolean()) "+" else "-"
                val a = random.nextInt(10, 50)
                val b = random.nextInt(5, 30)
                if (op == "+") {
                    val ans = a + b
                    MissionQuestion(
                        id = "math_easy_${a}_${b}_plus",
                        prompt = "$a + $b = ?",
                        questionType = "MATHS",
                        expectedAnswer = ans.toString(),
                        hint = "Calculate sum"
                    )
                } else {
                    val max = maxOf(a, b)
                    val min = minOf(a, b)
                    val ans = max - min
                    MissionQuestion(
                        id = "math_easy_${max}_${min}_minus",
                        prompt = "$max - $min = ?",
                        questionType = "MATHS",
                        expectedAnswer = ans.toString(),
                        hint = "Calculate difference"
                    )
                }
            }
            "HARD" -> {
                // Multi-step arithmetic: e.g. (a * b) + c or (a * b) - c or a * b * c
                val ops = listOf("mult_add", "mult_sub", "three_add")
                when (ops.random()) {
                    "mult_add" -> {
                        val a = random.nextInt(6, 15)
                        val b = random.nextInt(6, 14)
                        val c = random.nextInt(10, 50)
                        val ans = (a * b) + c
                        MissionQuestion(
                            id = "math_hard_${a}_${b}_${c}_ma",
                            prompt = "($a × $b) + $c = ?",
                            questionType = "MATHS",
                            expectedAnswer = ans.toString(),
                            hint = "Multiply first, then add"
                        )
                    }
                    "mult_sub" -> {
                        val a = random.nextInt(7, 15)
                        val b = random.nextInt(6, 12)
                        val c = random.nextInt(10, 40)
                        val ans = (a * b) - c
                        MissionQuestion(
                            id = "math_hard_${a}_${b}_${c}_ms",
                            prompt = "($a × $b) - $c = ?",
                            questionType = "MATHS",
                            expectedAnswer = ans.toString(),
                            hint = "Multiply first, then subtract"
                        )
                    }
                    else -> {
                        val a = random.nextInt(25, 90)
                        val b = random.nextInt(25, 90)
                        val c = random.nextInt(15, 60)
                        val ans = a + b + c
                        MissionQuestion(
                            id = "math_hard_${a}_${b}_${c}_ta",
                            prompt = "$a + $b + $c = ?",
                            questionType = "MATHS",
                            expectedAnswer = ans.toString(),
                            hint = "Sum of three numbers"
                        )
                    }
                }
            }
            else -> {
                // MEDIUM: multiplication or addition/subtraction with two-digit numbers
                val op = listOf("+", "-", "×").random()
                when (op) {
                    "×" -> {
                        val a = random.nextInt(4, 12)
                        val b = random.nextInt(4, 12)
                        val ans = a * b
                        MissionQuestion(
                            id = "math_med_${a}_${b}_mult",
                            prompt = "$a × $b = ?",
                            questionType = "MATHS",
                            expectedAnswer = ans.toString(),
                            hint = "Multiply numbers"
                        )
                    }
                    "+" -> {
                        val a = random.nextInt(25, 95)
                        val b = random.nextInt(18, 75)
                        val ans = a + b
                        MissionQuestion(
                            id = "math_med_${a}_${b}_plus",
                            prompt = "$a + $b = ?",
                            questionType = "MATHS",
                            expectedAnswer = ans.toString(),
                            hint = "Calculate sum"
                        )
                    }
                    else -> {
                        val a = random.nextInt(45, 120)
                        val b = random.nextInt(15, 44)
                        val ans = a - b
                        MissionQuestion(
                            id = "math_med_${a}_${b}_minus",
                            prompt = "$a - $b = ?",
                            questionType = "MATHS",
                            expectedAnswer = ans.toString(),
                            hint = "Calculate difference"
                        )
                    }
                }
            }
        }
    }

    private fun generateSequenceQuestion(difficulty: String): MissionQuestion {
        val random = Random.Default
        return when (difficulty.uppercase()) {
            "EASY" -> {
                val step = random.nextInt(2, 6)
                val start = random.nextInt(1, 15)
                val seq = listOf(start, start + step, start + step * 2, start + step * 3)
                val next = start + step * 4
                MissionQuestion(
                    id = "seq_easy_${start}_${step}",
                    prompt = "${seq.joinToString(", ")}, ___",
                    questionType = "SEQUENCE",
                    expectedAnswer = next.toString(),
                    hint = "Arithmetic step: +$step"
                )
            }
            "HARD" -> {
                // Geometric or alternating difference
                val start = random.nextInt(2, 5)
                val mul = random.nextInt(2, 3)
                val seq = listOf(start, start * mul, start * mul * mul, start * mul * mul * mul)
                val next = start * mul * mul * mul * mul
                MissionQuestion(
                    id = "seq_hard_${start}_${mul}",
                    prompt = "${seq.joinToString(", ")}, ___",
                    questionType = "SEQUENCE",
                    expectedAnswer = next.toString(),
                    hint = "Geometric step: ×$mul"
                )
            }
            else -> {
                // Medium: difference increases by 1 or 2 (e.g., +2, +4, +6)
                val start = random.nextInt(3, 10)
                val d = random.nextInt(2, 4)
                val s1 = start
                val s2 = s1 + d
                val s3 = s2 + (d * 2)
                val s4 = s3 + (d * 3)
                val next = s4 + (d * 4)
                MissionQuestion(
                    id = "seq_med_${start}_${d}",
                    prompt = "$s1, $s2, $s3, $s4, ___",
                    questionType = "SEQUENCE",
                    expectedAnswer = next.toString(),
                    hint = "Incremental series"
                )
            }
        }
    }

    private fun generateMemoryQuestion(difficulty: String): MissionQuestion {
        val random = Random.Default
        val length = when (difficulty.uppercase()) {
            "EASY" -> 4
            "HARD" -> 7
            else -> 5
        }
        val digits = (1..length).map { random.nextInt(0, 10) }.joinToString("")
        return MissionQuestion(
            id = "mem_${digits}_${System.currentTimeMillis() % 10000}",
            prompt = "Memorize & Type: $digits",
            questionType = "MEMORY",
            expectedAnswer = digits,
            hint = "Recall all $length digits"
        )
    }
}
