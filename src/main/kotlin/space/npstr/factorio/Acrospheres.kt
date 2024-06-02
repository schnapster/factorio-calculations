@file:Suppress("FunctionName", "unused", "DuplicatedCode", "NonAsciiCharacters", "PropertyName")

package space.npstr.factorio

import java.util.concurrent.ThreadLocalRandom

fun main() {
//	`2φ to 2λ`()
	val starter = Bag(
		ε = 2,
		λ = 1,
		φ = 1,
		ξ = 0,

		γ = 1,
		θ = 0,
		ω = 0,
		ζ = 0,
	)

	val networkSolver = BagNetworkSolver(starter)
	networkSolver.buildNetwork()

	val allBags = networkSolver.allBags
	val target = starter.copy(
		ε = 0,
//			λ = 2,
//			φ = 2,
		ξ = 2,

//			γ = 2,
//			θ = 2,
//			ω = 2,
//			ζ = 2,
	)
	val foundTarget = allBags.keys.find {
		it == target
	}
	println(foundTarget)

	// we can do transformations:
	//2 ε <-> 2 ξ
	//2 λ <-> 2 φ
	//
	//2 ω <-> 2 ζ
	//2 γ <-> 2 θ

	val path = networkSolver.findPath(
		starter, starter.copy(
			ε = starter.ε - 2,
			ξ = starter.ξ + 2,
		)
	)
	println(path.joinToString(", "))
}


fun `2φ to 2λ`() {
	val bag = Bag(
		ε = 3,
		λ = 2,
		φ = 4,
		ξ = 2,

		γ = 3,
		θ = 3,
		ω = 3,
		ζ = 2,
	)

	//goal: destroy one φ, create λ, ξ, or ζ

	println(bag)

	bag.`fold φ & γ to ω & ξ`()
	bag.`fold φ & γ to ω & ξ`()

	bag.`fold ε & ω to λ & γ`()
	bag.`fold λ & ω to ξ & θ`()
	bag.`fold ξ & ζ to θ & φ`()
	bag.`fold ξ & γ to ζ & λ`()
	bag.`fold θ & ε to φ & ω`()
	bag.`fold ζ & φ to γ & ε`()
	bag.`fold ζ & φ to γ & ε`()
	// need more ζ
	bag.`fold ξ & γ to ζ & λ`()
	bag.`fold λ & θ to ε & ζ`()

	bag.`fold ε & ω to λ & γ`()


	println(bag)
}

class BagNetworkSolver(
	private val starter: Bag,
) {

	val allBags = mutableMapOf<Bag, Set<Bag>>()

	fun buildNetwork() {
		allBags.clear()

		createNetworkFromBags(mutableSetOf(starter))

		println("Created network with ${allBags.size} nodes")
//		allBags.keys.forEach { println(it) }
	}

	private tailrec fun createNetworkFromBags(checkBags: MutableSet<Bag>) {
		val nextBag = checkBags.firstOrNull()
		// we done here boiz
			?: return
		checkBags.remove(nextBag)

//		println("Checking bag $nextBag")

		val possibleBags = possibleBags(nextBag)
		allBags[nextBag] = possibleBags

		val newBags = possibleBags.subtract(allBags.keys)

//		println("Found ${newBags.size} new bags to check")

		checkBags.addAll(newBags)

		createNetworkFromBags(checkBags)
	}

	private fun possibleBags(bag: Bag): Set<Bag> {
		return setOf(
			bag.copy().apply { `fold λ & θ to ε & ζ`() },
			bag.copy().apply { `fold ξ & γ to ζ & λ`() },
			bag.copy().apply { `fold θ & ε to φ & ω`() },
			bag.copy().apply { `fold ξ & ζ to θ & φ`() },
			bag.copy().apply { `fold φ & γ to ω & ξ`() },
			bag.copy().apply { `fold ζ & φ to γ & ε`() },
			bag.copy().apply { `fold ε & ω to λ & γ`() },
			bag.copy().apply { `fold λ & ω to ξ & θ`() },
		).filter { it != bag }.toSet()
	}

	fun findPath(from: Bag, to: Bag): List<Bag> {
		return searchPath(listOf(from), to) ?: listOf()
	}

	private fun searchPath(path: List<Bag>, to: Bag): List<Bag>? {
		val current = path.last()
		val options = allBags[current]!!

		for (option in options) {
			if (path.contains(option)) {
				return null // going in circles
			}

			val nextPath = path.toMutableList().apply { add(option) }
			if (option == to) {
				return nextPath // found it!
			}

			val foundPath = searchPath(nextPath, to)
			if (foundPath != null) return foundPath
		}

		return null // nothing found here
	}
}

data class Bag(
	var ε: Int,
	var λ: Int,
	var φ: Int,
	var ξ: Int,

	var γ: Int,
	var θ: Int,
	var ω: Int,
	var ζ: Int,
) {

	fun tesseract() {
		if (λ < 1 || ξ < 1 || ζ < 1) return

		λ--
		ξ--
		ζ--

		φ++
		if (ThreadLocalRandom.current().nextBoolean()) {
			θ++
			ε++
		} else {
			γ++
			ω++
		}
	}


	fun `fold λ & ω to ξ & θ`() {
		if (λ < 1 || ω < 1) return
		λ--
		ω--

		ξ++
		θ++
	}

	fun `fold ξ & γ to ζ & λ`() {
		if (ξ < 1 || γ < 1) return
		ξ--
		γ--

		ζ++
		λ++
	}

	fun `fold ξ & ζ to θ & φ`() {
		if (ξ < 1 || ζ < 1) return
		ξ--
		ζ--

		θ++
		φ++
	}

	fun `fold λ & θ to ε & ζ`() {
		if (λ < 1 || θ < 1) return
		λ--
		θ--

		ε++
		ζ++
	}

	fun `fold θ & ε to φ & ω`() {
		if (θ < 1 || ε < 1) return
		θ--
		ε--

		φ++
		ω++
	}

	fun `fold ζ & φ to γ & ε`() {
		if (ζ < 1 || φ < 1) return
		ζ--
		φ--

		γ++
		ε++
	}

	fun `fold φ & γ to ω & ξ`() {
		if (φ < 1 || γ < 1) return
		φ--
		γ--

		ω++
		ξ++
	}

	fun `fold ε & ω to λ & γ`() {
		if (ε < 1 || ω < 1) return
		ε--
		ω--

		λ++
		γ++
	}

	fun invertWhiteToGreen() {
		if (ε < 1
			|| λ < 1
			|| φ < 1
			|| ξ < 1
		) return

		ε--
		λ--
		φ--
		ξ--

		γ++
		θ++
		ω++
		ζ++
	}

	fun invertGreenToWhite() {
		if (γ < 1
			|| θ < 1
			|| ω < 1
			|| ζ < 1
		) return

		γ--
		θ--
		ω--
		ζ--

		ε++
		λ++
		φ++
		ξ++
	}

}
