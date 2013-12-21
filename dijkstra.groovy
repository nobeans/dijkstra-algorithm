@Grab(group='ch.qos.logback', module='logback-classic', version='1.0.13')
import groovy.util.logging.Slf4j

// Define Class
@Slf4j
class Node {
    def id
    def edges = []
    int cost = Integer.MAX_VALUE // 最小コスト
    Node from                    // 最小コストはどのノードからきたときか
    boolean done = false         // 評価済みフラグ

    String toString() {
        "${done ? '*' : ' '}node#${id}${edges}:${fromPath}"
    }

    String getFromPath() {
        (from ? from.fromPath : '') + "/node#${id}(${cost})"
    }

    // このノードを評価して隣接ノードのコストに反映する。
    void evalNode() {
        if (done) return
        log.debug "evalNode(): begin: ${this}"
        edges.each { edge ->
            // 各接続先ノードのコストがこのノード経由の方がコストが低いか？
            def toNode = edge.to
            int costToNodeFromThisNode = cost + edge.cost
            if (toNode.cost > costToNodeFromThisNode) {
                // 最小コストを更新する。
                toNode.cost = costToNodeFromThisNode
                toNode.from = this
                log.debug "evalNode(): updated edges node: ${toNode}"
            } else {
                log.debug "evalNode(): ignored edges node: ${toNode}"
            }
        }
        done = true
        log.debug "evalNode(): done: ${this}"
    }
}

@Slf4j
class NodeList {
    def nodes = [:]

    NodeList(Iterable range) {
        range.each {
            nodes[it] = new Node(id: it)
        }
    }

    def getAt(key) {
        nodes[key]
    }

    String toString() {
        nodes.collect { it }.join(System.getProperty("line.separator"))
    }

    void calculateCostFrom(startNode) {
        // スタートノードのコストは0
        startNode.cost = 0
        while (true) {
            // その時点の未評価＆最小コストのノードから順番に評価していく
            def node = minCostNode
            if (!node) return
            node.evalNode()
        }
    }

    private getMinCostNode() {
        nodes.findAll { id, node -> !node.done }.sort { it.value.cost }.find()?.value
    }
}

class Edge {
    Node to      // 接続先ノード
    int cost = 0 // 接続先ノードへの必要コスト

    String toString() {
        "(${cost})->node#${to?.id}"
    }
}

// Setup sample data
def nodes = new NodeList(1..6)

nodes[1].edges = [
    new Edge(cost: 5, to: nodes[2]),
    new Edge(cost: 4, to: nodes[3]),
    new Edge(cost: 2, to: nodes[4]),
]
nodes[2].edges = [
    new Edge(cost: 5, to: nodes[1]),
    new Edge(cost: 2, to: nodes[3]),
    new Edge(cost: 6, to: nodes[6]),
]
nodes[3].edges = [
    new Edge(cost: 4, to: nodes[1]),
    new Edge(cost: 2, to: nodes[2]),
    new Edge(cost: 3, to: nodes[4]),
    new Edge(cost: 2, to: nodes[5]),
]
nodes[4].edges = [
    new Edge(cost: 2, to: nodes[1]),
    new Edge(cost: 3, to: nodes[3]),
    new Edge(cost: 6, to: nodes[5]),
]
nodes[5].edges = [
    new Edge(cost: 2, to: nodes[3]),
    new Edge(cost: 6, to: nodes[4]),
    new Edge(cost: 4, to: nodes[6]),
]
nodes[6].edges = [
    new Edge(cost: 6, to: nodes[2]),
    new Edge(cost: 4, to: nodes[5]),
]

// Solving

def dumpNodes = { label = '' ->
    println "${label}" + "-"*10
    println nodes
}

// スタートとゴールの指定
def startNode = nodes[1]
def goalNode = nodes[6]

dumpNodes("Initial")

nodes.calculateCostFrom(startNode)

dumpNodes("Done")

// スタートからゴールまでの道を表示する
println "Answer:" + "-"*50
println goalNode.fromPath

