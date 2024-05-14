package org.hyperledger.identus.walletsdk.apollo.derivation

import kotlinx.serialization.Serializable

@Serializable
sealed class RawTestVectorBase {
    abstract val seed: String
    abstract val derivations: List<List<String>>
}

@Serializable
class RawTestVector(
    override val seed: String,
    override val derivations: List<List<String>>
) : RawTestVectorBase()

val bip32Vectors =
    """
    [
      {
        "seed" : "000102030405060708090a0b0c0d0e0f",
        "derivations" : [
          [
            "m",
            "0439a36013301597daef41fbe593a02cc513d0b55527ec2df1050e2e8ff49c85c23cbe7ded0e7ce6a594896b8f62888fdbc5c8821305e2ea42bf01e37300116281",
            "e8f32e723decf4051aefac8e2c93c9c5b214313817cdb01a1494b917c8436b35"
          ],
          [
            "m/0'",
            "045a784662a4a20a65bf6aab9ae98a6c068a81c52e4b032c0fb5400c706cfccc567f717885be239daadce76b568958305183ad616ff74ed4dc219a74c26d35f839",
            "edb2e14f9ee77d26dd93b4ecede8d16ed408ce149b6cd80b0715a2d911a0afea"
          ],
          [
            "m/0'/1",
            "04501e454bf00751f24b1b489aa925215d66af2234e3891c3b21a52bedb3cd711c008794c1df8131b9ad1e1359965b3f3ee2feef0866be693729772be14be881ab",
            "3c6cb8d0f6a264c91ea8b5030fadaa8e538b020f0a387421a12de9319dc93368"
          ],
          [
            "m/0'/1/2'",
            "0457bfe1e341d01c69fe5654309956cbea516822fba8a601743a012a7896ee8dc24310ef3676384179e713be3115e93f34ac9a3933f6367aeb3081527ea74027b7",
            "cbce0d719ecf7431d88e6a89fa1483e02e35092af60c042b1df2ff59fa424dca"
          ],
          [
            "m/0'/1/2'/2",
            "04e8445082a72f29b75ca48748a914df60622a609cacfce8ed0e35804560741d292728ad8d58a140050c1016e21f285636a580f4d2711b7fac3957a594ddf416a0",
            "0f479245fb19a38a1954c5c7c0ebab2f9bdfd96a17563ef28a6a4b1a2a764ef4"
          ],
          [
            "m/0'/1/2'/2/1000000000",
            "042a471424da5e657499d1ff51cb43c47481a03b1e77f951fe64cec9f5a48f7011cf31cb47de7ccf6196d3a580d055837de7aa374e28c6c8a263e7b4512ceee362",
            "471b76e389e528d6de6d816857e012c5455051cad6660850e58372a6c3e6e7c8"
          ]
        ]
      },
      {
        "seed" : "fffcf9f6f3f0edeae7e4e1dedbd8d5d2cfccc9c6c3c0bdbab7b4b1aeaba8a5a29f9c999693908d8a8784817e7b7875726f6c696663605d5a5754514e4b484542",
        "derivations" : [
          [
            "m",
            "04cbcaa9c98c877a26977d00825c956a238e8dddfbd322cce4f74b0b5bd6ace4a77bd3305d363c26f82c1e41c667e4b3561c06c60a2104d2b548e6dd059056aa51",
            "4b03d6fc340455b363f51020ad3ecca4f0850280cf436c70c727923f6db46c3e"
          ],
          [
            "m/0",
            "04fc9e5af0ac8d9b3cecfe2a888e2117ba3d089d8585886c9c826b6b22a98d12ea67a50538b6f7d8b5f7a1cc657efd267cde8cc1d8c0451d1340a0fb3642777544",
            "abe74a98f6c7eabee0428f53798f0ab8aa1bd37873999041703c742f15ac7e1e"
          ],
          [
            "m/0/2147483647'",
            "04c01e7425647bdefa82b12d9bad5e3e6865bee0502694b94ca58b666abc0a5c3b6c8bf5e8fbfc053205b45776963d148187d0aebf9c08bf2b253dc1cf5860fc19",
            "877c779ad9687164e9c2f4f0f4ff0340814392330693ce95a58fe18fd52e6e93"
          ],
          [
            "m/0/2147483647'/1",
            "04a7d1d856deb74c508e05031f9895dab54626251b3806e16b4bd12e781a7df5b9105b3150817d235e80ea17914dc9d6f542b1c5f4b16d8d98fe3c94fc0a67de89",
            "704addf544a06e5ee4bea37098463c23613da32020d604506da8c0518e1da4b7"
          ],
          [
            "m/0/2147483647'/1/2147483646'",
            "04d2b36900396c9282fa14628566582f206a5dd0bcc8d5e892611806cafb0301f0ecb53a1b24eda1117d6864f1dbaf2f92345a1cb52c70036e2a424b37c3d829b0",
            "f1c7c871a54a804afe328b4c83a1c33b8e5ff48f5087273f04efa83b247d6a2d"
          ],
          [
            "m/0/2147483647'/1/2147483646'/2",
            "044d902e1a2fc7a8755ab5b694c575fce742c48d9ff192e63df5193e4c7afe1f9c4597bb130cb16893607c6e7418c46be47b8f4a3ddbe5e6e71051393b1d673abe",
            "bb7d39bdb83ecf58f2fd82b6d918341cbef428661ef01ab97c28a4842125ac23"
          ]
        ]
      },
      {
        "seed" : "4b381541583be4423346c643850da4b320e46a87ae3d2a4e6da11eba819cd4acba45d239319ac14f863b8d5ab5a0d0c64d2e8a1e7d1457df2e5a3c51c73235be",
        "derivations" : [
          [
            "m",
            "04683af1ba5743bdfc798cf814efeeab2735ec52d95eced528e692b8e34c4e5669d2f2686ced96d375a75298f07ed30751e2a3f45e2d184b268d02c8d5dd6fbdb5",
            "00ddb80b067e0d4993197fe10f2657a844a384589847602d56f0c629c81aae32"
          ],
          [
            "m/0'",
            "046557fdda1d5d43d79611f784780471f086d58e8126b8c40acb82272a7712e7f259a34ffdc4c82e5cb68a96ccc6cb53e8765527148d1a85b52dfb8953d8d001fc",
            "491f7a2eebc7b57028e0d3faa0acda02e75c33b03c48fb288c41e2ea44e1daef"
          ]
        ]
      }
    ]
    """.trimIndent()
