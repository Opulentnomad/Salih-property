package com.rihla.english.core.engine

import com.rihla.english.core.model.CourseUnit
import com.rihla.english.core.model.NodeType

enum class NodeState { LOCKED, AVAILABLE, COMPLETED }

/** Unlock rules for the snake-path skill tree. */
object SkillPath {

    /**
     * Lessons unlock strictly in order. A chest becomes AVAILABLE (ready to open) once every
     * lesson before it is complete; chests never block progression.
     */
    fun nodeStates(units: List<CourseUnit>, completedNodeIds: Set<String>): Map<String, NodeState> {
        val states = LinkedHashMap<String, NodeState>()
        var previousLessonsDone = true
        units.sortedBy { it.orderIndex }.forEach { unit ->
            unit.nodes.sortedBy { it.orderIndex }.forEach { node ->
                val state = when {
                    node.id in completedNodeIds -> NodeState.COMPLETED
                    previousLessonsDone -> NodeState.AVAILABLE
                    else -> NodeState.LOCKED
                }
                states[node.id] = state
                if (node.type == NodeType.LESSON) {
                    previousLessonsDone = state == NodeState.COMPLETED
                }
            }
        }
        return states
    }

    /** The lesson the learner should do next (first AVAILABLE lesson), or null if all done. */
    fun currentLessonId(units: List<CourseUnit>, states: Map<String, NodeState>): String? =
        units.sortedBy { it.orderIndex }.flatMap { it.nodes.sortedBy { n -> n.orderIndex } }
            .firstOrNull { it.type == NodeType.LESSON && states[it.id] == NodeState.AVAILABLE }?.id

    /** Units whose lessons are all completed. */
    fun completedUnitIds(units: List<CourseUnit>, completedNodeIds: Set<String>): Set<String> =
        units.filter { unit ->
            unit.nodes.filter { it.type == NodeType.LESSON }.all { it.id in completedNodeIds }
        }.map { it.id }.toSet()
}
